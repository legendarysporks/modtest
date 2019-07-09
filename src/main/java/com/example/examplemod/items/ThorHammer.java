package com.example.examplemod.items;

import com.example.examplemod.utilities.commands.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;

public class ThorHammer extends GenericBlockGun {
	private static final String COMMAND_NAME = "ThorHammer";
	private static final String COMMAND_USAGE = "Try /ThorHammer settings";
	private static final String[] COMMAND_ALIASES = {"thorhammer", "thorHammer", "th"};
	private static Block effectBlock = Blocks.MAGMA;
	private static Block aboveEffectBlock = Blocks.FIRE;

	public ThorHammer(String name) {
		super(name, COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES);
	}

	public ThorHammer(String name, CreativeTabs tab) {
		super(name, COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES, tab);
	}

	private static boolean isSolid(World w, Vec3d v) {
		// water, and air are not solid
		IBlockState state = w.getBlockState(toBlockPos(v));
		return state.getMaterial().isSolid()
				&& (state.getBlock() != effectBlock)
				&& (state.getBlock() != aboveEffectBlock);
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

	private static boolean isBreakable(World world, Vec3d v) {
		return isBreakable(world, toBlockPos(v));
	}

	private static boolean isBreakable(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlockHardness(world, pos) >= 0;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		if (!worldIn.isRemote) {
			// get direction player is looking (normalized)
			Vec3d lookVec = entityLiving.getLookVec();
			// extend that to maximum range of gun
			Vec3d finishDistance = lookVec.scale(getRange());
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
	public void setLowerBlock(String blockName) {
		Block b = Block.getBlockFromName(blockName);
		if (b != null) {
			effectBlock = b;
		}
	}

	@Setting
	public String getUpperBlock() {
		return aboveEffectBlock.getRegistryName().toString();
	}

	@Setting
	public void setUpperBlock(String blockName) {
		Block b = Block.getBlockFromName(blockName);
		if (b != null) {
			aboveEffectBlock = b;
		}
	}
}
