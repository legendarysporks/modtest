package com.example.examplemod.thorhammer;

import com.example.examplemod.items.GenericItem;
import com.example.examplemod.utilities.GenericSettings.GenericCommandWithSettings;
import com.example.examplemod.utilities.GenericSettings.Setting;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class ThorHammer extends GenericItem {
	private static final Set<Material> materials = new HashSet<>(Arrays.asList(
			Material.GLASS,
			Material.GRASS,
			Material.GROUND,
			Material.IRON,
			Material.ROCK,
			Material.SAND,
			Material.WOOD,


			Material.WATER,
			Material.LAVA
	));
	private static final String COMMAND_NAME = "ThorHammer";
	private static final String COMMAND_USAGE = "Try /ThorHammer settings";
	private static final String[] COMMAND_ALIASES = {"thorhammer", "thorHammer"};
	private double range = 30;
	private int affectDurationInTicks = 3 * 20; // 10 seconds
	private int trailLength = 15;
	private long stepDurationInTicks = Math.round(affectDurationInTicks / range);
	private List<ThorHammerAffect> activeAffects = new ArrayList<>();
	private GenericCommandWithSettings command = new GenericCommandWithSettings(COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES, this);

	public ThorHammer(String name) {
		super(name);
	}

	public ThorHammer(String name, CreativeTabs tab) {
		super(name, tab);
	}

	@Setting
	public double getRange() {
		return range;
	}

	@Setting
	public void setRange(double range) {
		this.range = range;
		stepDurationInTicks = Math.round(affectDurationInTicks / range);
	}

	@Setting
	public int getAffectDurationInTicks() {
		return affectDurationInTicks;
	}

	@Setting
	public void setAffectDurationInTicks(int affectDurationInTicks) {
		this.affectDurationInTicks = affectDurationInTicks;
		stepDurationInTicks = Math.round(affectDurationInTicks / range);
	}

	@Setting
	public int getTrailLength() {
		return trailLength;
	}

	@Setting
	public void setTrailLength(int trailLength) {
		this.trailLength = trailLength;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		if (!worldIn.isRemote && shouldStartAffect(stack, worldIn, state, pos, entityLiving)) {
			// get direction player is looking (normalized)
			Vec3d lookVec = entityLiving.getLookVec();
			// start the affect from start to finish
			startAffect(worldIn, calculateStartPos(pos, lookVec), calculateFinishPos(pos, lookVec));
			return false;
		} else {
			return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
		}
	}

	protected boolean shouldStartAffect(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		return state.getBlockHardness(worldIn, pos) >= 0;
//		Material material = state.getMaterial();
//		return materials.contains(material);
	}

	protected BlockPos calculateStartPos(BlockPos pos, Vec3d lookVec) {
		return pos;
	}

	protected BlockPos calculateFinishPos(BlockPos pos, Vec3d lookVec) {
		// find the location at maximum range
		Vec3d finishDistance = lookVec.scale(range);
		// we're only moving parallel to the ground plane for now
		return new BlockPos(pos.getX() + finishDistance.x, pos.getY(), pos.getZ() + finishDistance.z);
	}

	protected void startAffect(World world, BlockPos start, BlockPos finish) {
		if (activeAffects.isEmpty()) {
			MinecraftForge.EVENT_BUS.register(this);
		}
		activeAffects.add(new ThorHammerAffect(world, start, finish));
	}

	@SubscribeEvent
	public void handleTickEvents(TickEvent.ServerTickEvent event) {
		List<ThorHammerAffect> finishedAffects = new ArrayList<>();
		for (ThorHammerAffect affect : activeAffects) {
			if (!affect.doStep()) {
				finishedAffects.add(affect);
			}
		}
		activeAffects.removeAll(finishedAffects);
		if (activeAffects.isEmpty()) {
			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}

	protected void handleStartPosition(World world, BlockPos pos) {
	}

	protected boolean handleAddPosition(World world, BlockPos pos) {
		world.setBlockState(pos, Blocks.MAGMA.getDefaultState());
		BlockPos above = pos.up();
		if (world.getBlockState(above).getMaterial() == Material.AIR) {
			world.setBlockState(above, Blocks.FIRE.getDefaultState());
		}
		return true;
	}

	protected void handleRemovePosition(World world, BlockPos pos) {
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		BlockPos above = pos.up();
		if (world.getBlockState(above).getMaterial() == Material.AIR) {
			world.setBlockToAir(above);
		}
	}


	protected void handleFinishPosition(World world, BlockPos pos) {
	}

	/**
	 * A ThorHammerAffect is created whenever a ThorHammer is used to break a block.  Each tracks
	 * a dynamic affect over time that takes place between a point of origin and a destination point.
	 */
	private class ThorHammerAffect {
		private World world;
		private int totalSteps;
		private int currentStep;
		private Vec3d stepSize;
		private BlockPos startPos;
		private BlockPos finishPos;
		private Queue<BlockPos> positionsVisited = new ArrayDeque<>(trailLength);
		private long ticksToNextStep = 0;

		public ThorHammerAffect(World world, BlockPos start, BlockPos finish) {
			this.world = world;
			startPos = start;
			finishPos = finish;
			int dx = (finish.getX() - start.getX());
			int dy = (finish.getY() - start.getY());
			int dz = (finish.getZ() - start.getZ());
			// We want to step one pixel at a time along the longest axis
			totalSteps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
			stepSize = new Vec3d((double) dx / totalSteps, (double) dy / totalSteps, (double) dz / totalSteps);
			currentStep = 0;
			handleStartPosition(world, start);
		}

		public boolean doStep() {
			// terminate the whole thing is we've done all our steps & undone all our stuff
			if (currentStep > totalSteps && positionsVisited.isEmpty()) return false;

			// if we haven't waited long enough to do the next step, just exit
			if (ticksToNextStep-- > 0) return true;

			// An entire stepDurationInTicks has elapsed, so reset timer and do stuff
			ticksToNextStep = stepDurationInTicks;

			if (positionsVisited.size() >= trailLength) {
				// We've reached the max trail size, remove an old pixel so we can add a new one.
				handleRemovePosition(world, positionsVisited.remove());
			}
			if (currentStep < totalSteps) {
				// We haven't reached the finish yet, add a pixel
				// calculate the next pixel location
				BlockPos nextPos = startPos.add(stepSize.x * currentStep, stepSize.y * currentStep, stepSize.z * currentStep);
				currentStep = currentStep + 1;
				// if this position hasn't been touched yet (which might happen due to round off errors, twiddle the block
				if (positionsVisited.contains(nextPos)) {
					// pixel has already been handled so just continue
					return true;
				} else {
					// add a new pixel to the list and handle it
					positionsVisited.add(nextPos);
					return handleAddPosition(world, nextPos);
				}
			} else if (!positionsVisited.isEmpty()) {
				// we've reached the end so now we just remove positionsVisited along the line 1 by 1
				// until they are all gone
				handleRemovePosition(world, positionsVisited.remove());
				return true;
			} else {
				// We've reached the finish and we've removed all positionsVisited remaining.   Done.
				handleFinishPosition(world, finishPos);
				return false;
			}
		}
	}
}
