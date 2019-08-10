package com.example.examplemod.thorhammer;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.Reference;
import com.example.examplemod.init.ModItems;
import com.example.examplemod.utilities.RendererHelper;
import com.example.examplemod.utilities.Sparkles;
import com.example.examplemod.utilities.commands.InvalidValueException;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class ThorHammerProjectile extends EntityThrowable implements IEntityAdditionalSpawnData {
	private static final String NBT_TAG = Reference.MODID + ".ThorHammerProjectile";
	private static final String NAME = "thor_hammer_projectile";
	private static final int ID = 121;
	private static final float GRAVITY = 0.0f;
	private static final int LIFETIME_TICKS = 20;
	private static final float EXPLOSION_STRENGTH = 1.75F;
	private static final float DAMAGE = 100f;
	private static final float CATCH_DISTANCE = 1.0f;
	// provide some reference to the renderer so it's class is loaded/constructed/registered
	private static final RendererHelper renderer = ThorHammerProjectileRenderer.proxy;
	private static Block replacementBlock = Blocks.AIR;
	private static EnumParticleTypes replacementEffect = EnumParticleTypes.LAVA;
	private static int bounces = 1;
	private static EnumParticleTypes sparkleType = EnumParticleTypes.FLAME;

	private int bouncesRemaining = bounces;
	private EnumHand handIn;
	private float initialPitch;
	private float initialYaw;
	private double x;
	private double y;
	private double z;
	private float velocity;
	private float inaccuracy;
	private float exlosionStrength = EXPLOSION_STRENGTH;

	public ThorHammerProjectile(World world) {
		super(world);
	}

	public ThorHammerProjectile(World world, EntityPlayer entity, EnumHand handIn, float pitch, float yaw) {
		super(world, entity);
		this.handIn = handIn;
		this.initialPitch = pitch;
		this.initialYaw = yaw;
	}

	public static void registerModEntity() {
		EntityRegistry.registerModEntity(
				new ResourceLocation(Reference.MODID, NAME),
				ThorHammerProjectile.class,
				NAME,
				ID,
				ExampleMod.instance,
				64,
				10,
				true);
	}

	//----------------------------------------------------------------------------------------------------
	// for use by @Settings methods
	//----------------------------------------------------------------------------------------------------
	public float getInitialPitch() {
		return initialPitch;
	}

	public float getInitialYaw() {
		return initialYaw;
	}

	public static String getDamageBlock() {
		return replacementBlock.getRegistryName().toString();
	}

	public static void setDamageBlock(String blockName) throws InvalidValueException {
		Block b = Block.getBlockFromName(blockName);
		if (b != null) {
			replacementBlock = b;
		} else {
			throw new InvalidValueException("destroyedAffectBlock", blockName, "Block not found");
		}
	}

	public static String getDamageAffect() {
		return replacementEffect.getParticleName();
	}

	public static void setDamageAffect(String replacementAffectName) throws InvalidValueException {
		EnumParticleTypes newType = EnumParticleTypes.getByName(replacementAffectName);
		if (newType != null) {
			replacementEffect = newType;
		} else {
			throw new InvalidValueException("sparkleType", replacementAffectName, "Unknown particle type");
		}
	}

	public static int getBounces() {
		return bounces;
	}

	public static void setBounces(int newBounces) {
		bounces = newBounces;
	}

	public static String getSparkle() {
		return sparkleType.getParticleName();
	}

	public static void setSparkle(String newSparkleTypeName) throws InvalidValueException {
		EnumParticleTypes newType = EnumParticleTypes.getByName(newSparkleTypeName);
		if (newType != null) {
			sparkleType = newType;
		} else {
			throw new InvalidValueException("sparkleType", newSparkleTypeName, "Unknown particle type");
		}
	}

	//----------------------------------------------------------------------------------------------------
	// Client/Server syncing and persistence
	//----------------------------------------------------------------------------------------------------

	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeFloat(initialPitch);
		buffer.writeFloat(initialYaw);
	}

	public void readSpawnData(ByteBuf buffer) {
		initialPitch = buffer.readFloat();
		initialYaw = buffer.readFloat();
	}

	public NBTTagCompound writeToNBT(NBTTagCompound entityCompound) {
		super.writeEntityToNBT(entityCompound);
		entityCompound.setTag(NBT_TAG, writeLocalDataToNBT(new NBTTagCompound()));
		return entityCompound;
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey(NBT_TAG, Constants.NBT.TAG_COMPOUND)) {
			readLocalDataFromNBT(compound.getCompoundTag(NBT_TAG));
		}
	}

	protected NBTTagCompound writeLocalDataToNBT(NBTTagCompound compound) {
		compound.setInteger("bouncesRemaining", bouncesRemaining);
		compound.setString("handIn", handIn.toString());
		compound.setDouble("x", x);
		compound.setDouble("y", y);
		compound.setDouble("z", z);
		compound.setFloat("initialPitch", initialPitch);
		compound.setFloat("initialYaw", initialYaw);
		compound.setFloat("velocity", velocity);
		compound.setFloat("inaccuracy", inaccuracy);
		compound.setString("replacementBlock", getDamageBlock());
		return compound;
	}

	protected void readLocalDataFromNBT(NBTTagCompound compound) {
		bouncesRemaining = compound.getInteger("bouncesRemaining");
		handIn = EnumHand.valueOf(compound.getString("handIn"));
		x = compound.getDouble("x");
		y = compound.getDouble("y");
		z = compound.getDouble("z");
		initialPitch = compound.getFloat("initialPitch");
		initialYaw = compound.getFloat("initialYaw");
		velocity = compound.getFloat("velocity");
		inaccuracy = compound.getFloat("inaccuracy");
		try {
			setDamageBlock(compound.getString("replacementBlock"));
		} catch (InvalidValueException e) {
			// just ignore this.  Nothing we can do.
		}
	}

	//----------------------------------------------------------------------------------------------------
	// Behavior
	//----------------------------------------------------------------------------------------------------

	public void throwHammer(Entity entityThrower, float velocity, float inaccuracy) {
		shoot(entityThrower, entityThrower.rotationPitch, entityThrower.rotationYaw, 0.0f, velocity, inaccuracy);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (ticksExisted > LIFETIME_TICKS) {
			ticksExisted = 0;
			reverseDirection();
		}
		if (ticksExisted % 5 == 0) { // don't sparkle every tick
			Sparkles.yay(world, posX, posY, posZ, sparkleType);
		}
	}

	@Override
	protected float getGravityVelocity() {
		return GRAVITY;
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		super.shoot(x, y, z, velocity, inaccuracy);
		this.x = x;
		this.y = y;
		this.z = z;
		this.velocity = velocity;
		this.inaccuracy = inaccuracy;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		/* The contents of a RayTraceResult differ depending on its Type field:
				BLOCK
					blockPos
					sideHit
					hitVec
				ENTITY
					entityHit
					hitVec
				MISS
		 */

		if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
			if (result.entityHit == getThrower()) {
				catchHammer((EntityPlayer) result.entityHit);
				exlosionStrength = 0;
			} else {
				result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), DAMAGE);
//				Entity victim = result.entityHit;
//				world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, victim.posX, victim.posY, victim.posZ, 1.0D, 0.0D, 0.0D);
			}
		} else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = result.getBlockPos();
			world.setBlockState(pos, replacementBlock.getDefaultState());
			if (replacementBlock == Blocks.AIR) {
				Sparkles.yay(world, posX, posY, posZ, replacementEffect);
			}
//		} else if (result.typeOfHit == RayTraceResult.Type.MISS) {
		}
	}

	private void reverseDirection() {
		if (!world.isRemote && !isDead) {
			if (getThrower() != null && this.getThrower().getDistance(this) <= CATCH_DISTANCE) {
				// we're within the catch distance.
				catchHammer((EntityPlayer) this.getThrower());
			} else {
				if (bouncesRemaining-- > 0) {
					// we still have bouncing to do
					shoot(-x, -y, -z, velocity, inaccuracy);
				} else {
					// don't know who the throw is, so BOOM!
					explode();
				}
			}
		}
	}

	private void explode() {
		if (exlosionStrength > 0) {
			this.world.createExplosion(this, posX, posY, posZ, exlosionStrength, true);
		} else {
			Sparkles.yay(world, posX, posY, posZ, sparkleType);
		}
		setDead();
	}

	private void catchHammer(EntityPlayer player) {
		// Note that this will trash whatever was in this hand if it wasn't empty.
		// Thor's hammer waits for no hand.
		player.setHeldItem(player.getActiveHand(), new ItemStack(ModItems.thor_hammer));
		setDead();
	}
}