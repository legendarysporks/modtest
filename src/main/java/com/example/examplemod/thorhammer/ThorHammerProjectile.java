package com.example.examplemod.thorhammer;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.Reference;
import com.example.examplemod.init.ModItems;
import com.example.examplemod.utilities.Sparkles;
import com.example.examplemod.utilities.commands.InvalidValueException;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ThorHammerProjectile extends EntityThrowable {
	private static final String NAME = "thor_hammer_projectile";
	private static final int ID = 121;
	private static final float GRAVITY = 0.0f;
	private static final int LIFETIME_TICKS = 20;
	private static final float EXPLOSION_STRENGTH = 1.75F;
	private static final float DAMAGE = 100f;
	private static final float CATCH_DISTANCE = 2.0f;
	// provide some reference to the renderer so it's class is loaded/constructed/registered
	private static final ThorHammerProjectileRenderer renderer = ThorHammerProjectileRenderer.proxy.init();
	private static Block replacementBlock = Blocks.FIRE;
	private static int bounces = 1;
	private static EnumParticleTypes sparkleType = EnumParticleTypes.FLAME;

	private int bouncesRemaining = bounces;
	private EnumHand handIn;
	private double x;
	private double y;
	private double z;
	private float velocity;
	private float inaccuracy;
	private float exlosionStrength = EXPLOSION_STRENGTH;

	public ThorHammerProjectile(World world) {
		super(world);
	}

	public ThorHammerProjectile(World world, EntityPlayer entity, EnumHand handIn) {
		super(world, entity);
		this.handIn = handIn;
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

	public static String getDestroyedAffectBlock() {
		return replacementBlock.getRegistryName().toString();
	}

	public static void setDestroyedAffectBlock(String blockName) throws InvalidValueException {
		Block b = Block.getBlockFromName(blockName);
		if (b != null) {
			replacementBlock = b;
		} else {
			throw new InvalidValueException("destroyedAffectBlock", blockName, "Block not found");
		}
	}

	public static int getBounces() {
		return bounces;
	}

	public static void setBounces(int newBounces) {
		bounces = newBounces;
	}

	public static String getSparkleType() {
		return sparkleType.toString();
	}

	public static void setSparkleType(String newSparkleTypeName) throws InvalidValueException {
		EnumParticleTypes newType = EnumParticleTypes.getByName(newSparkleTypeName);
		if (newType != null) {
			sparkleType = newType;
		} else {
			new InvalidValueException("sparkleType", newSparkleTypeName, "Unknown particle type");
		}
	}

	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger(Reference.MODID + "ThorHammerProjectile.bouncesRemaining", bouncesRemaining);
		compound.setString(Reference.MODID + "ThorHammerProjectile.handIn", handIn.toString());
		compound.setDouble(Reference.MODID + "ThorHammerProjectile.x", x);
		compound.setDouble(Reference.MODID + "ThorHammerProjectile.y", y);
		compound.setDouble(Reference.MODID + "ThorHammerProjectile.z", z);
		compound.setFloat(Reference.MODID + "ThorHammerProjectile.velocity", velocity);
		compound.setFloat(Reference.MODID + "ThorHammerProjectile.inaccuracy", inaccuracy);
		compound.setString(Reference.MODID + "ThorHammerProjectile.replacementBlock", getDestroyedAffectBlock());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		bouncesRemaining = compound.getInteger(Reference.MODID + "ThorHammerProjectile.bouncesRemaining");
		handIn = EnumHand.valueOf(compound.getString(Reference.MODID + "ThorHammerProjectile.handIn"));
		x = compound.getDouble(Reference.MODID + "ThorHammerProjectile.x");
		y = compound.getDouble(Reference.MODID + "ThorHammerProjectile.y");
		z = compound.getDouble(Reference.MODID + "ThorHammerProjectile.z");
		velocity = compound.getFloat(Reference.MODID + "ThorHammerProjectile.velocity");
		inaccuracy = compound.getFloat(Reference.MODID + "ThorHammerProjectile.inaccuracy");
		try {
			setDestroyedAffectBlock(compound.getString(Reference.MODID + "ThorHammerProjectile.replacementBlock"));
		} catch (InvalidValueException e) {
			// just ignore this.  Nothing we can do.
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (ticksExisted > LIFETIME_TICKS) {
			ticksExisted = 0;
			reverseDirection();
		}
		Sparkles.yay(world, posX, posY, posZ, sparkleType);
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

		if (!world.isRemote) {
			if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
				if (result.entityHit == getThrower()) {
					EntityPlayer player = (EntityPlayer) getThrower();
					ItemStack itemstack = player.getHeldItem(handIn);
					// Note that this will trash whatever was in this hand if it wasn't empty.
					// Thor's hammer waits for no hand.
					player.setHeldItem(handIn, new ItemStack(ModItems.thor_hammer));
					setDead();
					exlosionStrength = 0;
				} else {
					result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), DAMAGE);
				}
			} else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
				world.setBlockState(result.getBlockPos(), replacementBlock.getDefaultState());
			} else if (result.typeOfHit == RayTraceResult.Type.MISS) {
			}
		}
	}

	private void reverseDirection() {
		if (!world.isRemote && !isDead) {
			if (bouncesRemaining-- > 0) {
				// we still have bouncing to do
				shoot(-x, -y, -z, velocity, inaccuracy);
			} else if ((this.getThrower() != null) && this.getThrower().getDistance(this) > CATCH_DISTANCE) {
				// we're done bouncing and not within catch distance of the thrower.
				explode();
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
}