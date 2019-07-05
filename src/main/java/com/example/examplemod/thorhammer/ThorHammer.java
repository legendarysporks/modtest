package com.example.examplemod.thorhammer;

import com.example.examplemod.utilities.GenericTrailGun;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThorHammer extends GenericTrailGun {
	private static final String COMMAND_NAME = "ThorHammer";
	private static final String COMMAND_USAGE = "Try /ThorHammer settings";
	private static final String[] COMMAND_ALIASES = {"thorhammer", "thorHammer"};

	public ThorHammer(String name) {
		super(name, COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES);
	}

	public ThorHammer(String name, CreativeTabs tab) {
		super(name, COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES, tab);
	}

	@Override
	protected BlockPos calculateEndPos(BlockPos pos, Vec3d lookVec) {
		// we overrode this so that things only move horizontally
		// find the location at maximum range
		Vec3d finishDistance = lookVec.scale(getRange());
		// we're only moving parallel to the ground plane for now
		return new BlockPos(pos.getX() + finishDistance.x, pos.getY(), pos.getZ() + finishDistance.z);
	}

	/** add initial affect to location. */
	@Override
	protected boolean handleAddPosition(World world, BlockPos pos) {
		// did we hit an unbreakable block or are we above air or water?
		boolean isBreakable = world.getBlockState(pos).getBlockHardness(world, pos) >= 0;

		if (isBreakable) {
			// the block is breakable so turn it to magma and set it on fire.
			world.setBlockState(pos, Blocks.MAGMA.getDefaultState());
			BlockPos above = pos.up();
			if (world.getBlockState(above).getMaterial() == Material.AIR) {
				world.setBlockState(above, Blocks.FIRE.getDefaultState());
			}
			return true;
		} else {
			// we hit an unbreakable block, so stop the whole process here.
			return false;
		}
	}

	/** add final affect to the location */
	@Override
	protected void handleRemovePosition(World world, BlockPos pos) {
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		BlockPos above = pos.up();
		if (world.getBlockState(above).getMaterial() == Material.AIR) {
			world.setBlockToAir(above);
		}
	}
}
