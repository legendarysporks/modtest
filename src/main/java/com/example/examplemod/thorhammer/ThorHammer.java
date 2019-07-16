package com.example.examplemod.thorhammer;

import com.example.examplemod.items.GenericBlockGun;
import com.example.examplemod.utilities.ClassUtils;
import com.example.examplemod.utilities.InventoryUtils;
import com.example.examplemod.utilities.commands.InvalidValueException;
import com.example.examplemod.utilities.commands.Setting;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class ThorHammer extends GenericBlockGun implements HackFMLEventListener {
	private static final String COMMAND_NAME = "ThorHammer";
	private static final String COMMAND_USAGE = "Try /ThorHammer settings";
	private static final String[] COMMAND_ALIASES = {"thorhammer", "thorHammer", "th"};
	private static Block effectBlock = Blocks.MAGMA;
	private static Block aboveEffectBlock = Blocks.FIRE;
	private static Class<? extends EntityThrowable> projectileClass = ThorHammerProjectile.class;
	private static Class<? extends Item> ammoClass = ThorHammer.class;
	private static float VELOCITY = 1.0f;
	private static float INACCURACY = 0.0f;

	public ThorHammer(String name) {
		super(name, COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES);
		subscribeToFMLEvents();
	}

	public ThorHammer(String name, CreativeTabs tab) {
		super(name, COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES, tab);
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
//		for (int i = 0; i < EMP_SOUND_NAMES.length; i++) {
//			EMPSounds[i] = createSoundEvent(EMP_SOUND_NAMES[i]);
//		}
		ThorHammerProjectile.registerModEntity();
	}


	private static boolean isSolid(World w, Vec3d v) {
		// water, and air are not solid
		IBlockState state = w.getBlockState(toBlockPos(v));
		return state.getMaterial().isSolid()
				&& (state.getBlock() != effectBlock)
				&& (state.getBlock() != aboveEffectBlock);
	}

	private static boolean isBreakable(World world, Vec3d v) {
		return isBreakable(world, toBlockPos(v));
	}

	private static boolean isBreakable(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlockHardness(world, pos) >= 0;
	}

	@Override
	protected Vec3d calculateNextPosition(World world, Collection<BlockPos> prevPos, Vec3d currentPos, Vec3d stepSize) {
		Vec3d abovePos = currentPos.addVector(0.0d, 1.0d, 0.0d);
		Vec3d forwardPos = currentPos.add(stepSize);
		Vec3d belowPos = currentPos.subtract(0.0d, 1.0d, 0.0d);
		Vec3d nextPos;
		if (isSolid(world, abovePos) && !prevPos.contains(toBlockPos(abovePos))) {
			nextPos = abovePos;
		} else if (isSolid(world, forwardPos)) {
			nextPos = forwardPos;
		} else if (isSolid(world, belowPos)) {
			nextPos = belowPos;
		} else {
			// we can't move.  We're stuck.  So stop messing around and finish
			nextPos = null;
		}

		// we know where we want to go.  Check if we can go there.
		if (nextPos != null && isBreakable(world, nextPos)) {
			return nextPos;
		} else {
			return null;
		}
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		if (!worldIn.isRemote) {
			// get direction player is looking (normalized)
			Vec3d lookVec = entityLiving.getLookVec();
			// extend that to maximum range of gun
			Vec3d finishDistance = lookVec.scale(getCrashRange());
			// start the affect from start to finish
			startAffect(worldIn, pos, new BlockPos(pos.getX() + finishDistance.x, pos.getY(), pos.getZ() + finishDistance.z));
			return false;
		} else {
			return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
		}
	}

	/** add initial affect to location. */
	@Override
	protected void handleAddPosition(World world, BlockPos pos) {
		// turn block to magma and set it on fire.
		world.setBlockState(pos, effectBlock.getDefaultState());
		BlockPos above = pos.up();
		if (world.getBlockState(above).getMaterial() == Material.AIR) {
			world.setBlockState(above, aboveEffectBlock.getDefaultState());
		}
	}

	/** add final affect to the location */
	@Override
	protected void handleRemovePosition(World world, BlockPos pos) {
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		BlockPos above = pos.up();
		if (world.getBlockState(above).getBlock() == aboveEffectBlock) {
			world.setBlockToAir(above);
		}
	}

	@Setting
	public String getLowerBlock() {
		return effectBlock.getRegistryName().toString();
	}

	@Setting
	public void setLowerBlock(String blockName) throws InvalidValueException {
		Block b = Block.getBlockFromName(blockName);
		if (b != null) {
			effectBlock = b;
		} else {
			throw new InvalidValueException("lowerBlock", blockName, "Not a block");
		}
	}

	@Setting
	public String getUpperBlock() {
		return aboveEffectBlock.getRegistryName().toString();
	}

	@Setting
	public void setUpperBlock(String blockName) throws InvalidValueException {
		Block b = Block.getBlockFromName(blockName);
		if (b != null) {
			aboveEffectBlock = b;
		} else {
			throw new InvalidValueException("upperBlock", blockName, "Not a block");
		}
	}

	@Setting
	public String getAmmoItem() {
		return ammoClass.getName();
	}

	@Setting
	public void setAmmoItem(String ammo) throws InvalidValueException {
		Class<? extends Item> clazz = ClassUtils.findItemClass(ammo);
		if (clazz != null) {
			ammoClass = clazz;
		} else {
			throw new InvalidValueException("ammoItem", ammo, "Not an item");
		}
	}

	@Setting
	public String getProjectileEntity() {
		return projectileClass.getName();
	}

	@Setting
	public void setProjectileEntity(String blockName) throws InvalidValueException {
		Class<? extends Entity> projClazz = ClassUtils.findEntityClass(blockName, EntityThrowable.class);
		if (projClazz != null) {
			Class<? extends EntityThrowable> projEntityClass = (Class<? extends EntityThrowable>) projClazz;
			try {
				projEntityClass.getConstructor(World.class, EntityLivingBase.class);
				projectileClass = projEntityClass;
			} catch (NoSuchMethodException e) {
				// doesn't have the required constructor
				throw new InvalidValueException("projectileEntity", blockName, "Entity with proper constructor not found");
			}
		} else {
			throw new InvalidValueException("projectileEntity", blockName, "EntityThrowable not found");
		}
	}

	@Setting
	public String getDamageBlock() {
		return ThorHammerProjectile.getDestroyedAffectBlock();
	}

	@Setting
	public void setDamageBlock(String blockName) throws InvalidValueException {
		ThorHammerProjectile.setDestroyedAffectBlock(blockName);
	}

	@Setting
	public int getBounces() {
		return ThorHammerProjectile.getBounces();
	}

	@Setting
	public void setBounces(int bounces) {
		ThorHammerProjectile.setBounces(bounces);
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		ItemStack itemstack = player.getHeldItem(handIn);
		ItemStack ammo = InventoryUtils.findInInventory(player, ammoClass);

		if ((ammo != null) && !ammo.isEmpty()) {
			player.setActiveHand(handIn);
			ammo.grow(-1);
			player.swingArm(handIn);
//			int soundNumber = player.world.rand.nextInt(EMPSounds.length);
//			SoundEvent sound = EMPSounds[soundNumber];
//			world.playSound(player, player.getPosition(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
			if (!world.isRemote) {
				try {
					Constructor<? extends EntityThrowable> constructor =
							projectileClass.getConstructor(World.class, EntityPlayer.class, EnumHand.class);
					EntityThrowable projectile = (EntityThrowable) constructor.newInstance(world, player, handIn);
					projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, VELOCITY, INACCURACY);
					world.spawnEntity(projectile);
				} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
					return new ActionResult<>(EnumActionResult.FAIL, itemstack);
				}
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		} else {
			// out of ammo.  FAIL.
			return new ActionResult<>(EnumActionResult.FAIL, itemstack);
		}
	}
}
