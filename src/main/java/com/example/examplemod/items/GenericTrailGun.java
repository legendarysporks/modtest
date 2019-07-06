package com.example.examplemod.items;

import com.example.examplemod.utilities.commands.GenericCommand;
import com.example.examplemod.utilities.commands.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class GenericTrailGun extends GenericItem {
	private static final String CONFIG_VERSION = "0.1";
	private final List<TrailGunAffect> activeAffects = new ArrayList<>();
	private double range = 30;
	private int affectDurationInTicks = 3 * 20; // 10 seconds
	private int trailLength = 15;
	private long stepDurationInTicks = Math.round(affectDurationInTicks / range);

	public GenericTrailGun(String name, String commandName, String usage, String[] aliases) {
		super(name);
		GenericCommand.create(commandName, usage, aliases).addTargetWithPersitentSettings(this, commandName, CONFIG_VERSION);
	}

	public GenericTrailGun(String name, String commandName, String usage, String[] aliases, CreativeTabs tab) {
		super(name, tab);
		GenericCommand.create(commandName, usage, aliases).addTargetWithPersitentSettings(this, commandName, CONFIG_VERSION);
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
	public int getDurationTicks() {
		return affectDurationInTicks;
	}

	@Setting
	public void setDurationTicks(int affectDurationInTicks) {
		this.affectDurationInTicks = affectDurationInTicks;
		stepDurationInTicks = Math.round(affectDurationInTicks / range);
	}

	@Setting
	public double getVelocity() {
		return range / affectDurationInTicks;
	}

	@Setting
	public void setVelocity(double velocity) {
		setDurationTicks((int) Math.round(range / velocity));
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
			startAffect(worldIn, calculateStartPos(pos, lookVec), calculateEndPos(pos, lookVec));
			return false;
		} else {
			return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
		}
	}

	/** start the tick callback if it isn't running and add an affect */
	private void startAffect(World world, BlockPos start, BlockPos finish) {
		if (activeAffects.isEmpty()) {
			MinecraftForge.EVENT_BUS.register(this);
		}
		activeAffects.add(new TrailGunAffect(world, start, finish));
	}

	/** tick.  Deal with affect */
	@SubscribeEvent
	public void handleTickEvents(TickEvent.ServerTickEvent event) {
		List<TrailGunAffect> finishedAffects = new ArrayList<>();
		for (TrailGunAffect affect : activeAffects) {
			if (!affect.doStep()) {
				finishedAffects.add(affect);
			}
		}
		activeAffects.removeAll(finishedAffects);
		if (activeAffects.isEmpty()) {
			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}

	//----------------------------------------------------------------------------------------
	// subclass interface

	// A block is destroyed
	// if (shouldStartAffect)
	//   calculateStartPos
	//   calculateEndPos
	//   handleStartPosition - you get a chance to do something at the starting block
	//   while not at end position
	//     handleRemovePosition - cleanup previously added positions that cause the list of
	//             visitedLocations to grow to be greater than trailLength
	//     handleAddPosition - add next position along path to end position and do what we
	//             want to that location
	//   endwhile
	//   handleRemovePosition - cleanup any positions that haven't been cleaned up yet
	//   handleFinishPosition
	// endif

	/** override to start affect under different conditions */
	protected boolean shouldStartAffect(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		return state.getBlockHardness(worldIn, pos) >= 0;
	}

	/** By default, affects start at the block being destroyed */
	protected BlockPos calculateStartPos(BlockPos pos, Vec3d lookVec) {
		return pos;
	}

	/** By defaul, affects move in the direction the player is looking to the set distance set by range */
	protected BlockPos calculateEndPos(BlockPos pos, Vec3d lookVec) {
		// find the location at maximum range
		Vec3d finishDistance = lookVec.scale(range);
		// we're only moving parallel to the ground plane for now
		return new BlockPos(pos.getX() + finishDistance.x, pos.getY() + finishDistance.y, pos.getZ() + finishDistance.z);
	}

	/** Called once when the affect starts */
	protected void handleStartPosition(World world, BlockPos pos) {
	}

	/** add initial affect to location.  Return true if the affect should continue going */
	protected boolean handleAddPosition(World world, BlockPos pos) {
		return true;
	}

	/** cleanup affect previously at the given location */
	protected void handleRemovePosition(World world, BlockPos pos) {
	}

	/** add final affect to the location */
	protected void handleFinishPosition(World world, BlockPos pos) {
	}

	//----------------------------------------------------------------------------------------

	/**
	 * A TrailGunAffect is created whenever a ThorHammer is used to break a block.  Each tracks
	 * a dynamic affect over time that takes place between a point of origin and a destination point.
	 */
	private class TrailGunAffect {
		private World world;
		private int totalSteps;
		private int currentStep;
		private Vec3d stepSize;
		private BlockPos startPos;
		private BlockPos finishPos;
		private Queue<BlockPos> positionsVisited = new ArrayDeque<>(trailLength);
		private long ticksToNextStep = 0;

		public TrailGunAffect(World world, BlockPos start, BlockPos finish) {
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
					if (!handleAddPosition(world, nextPos)) {
						// end() just puts us in a position to clean up our existing mess
						end();
						// we fall through and return true because there is probably still cleanup to do
						// but because we called end(), no more new positions will be added.
					}
					return true;
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

		/** stop creating more mess and start cleaning up what's there */
		public void end() {
			ticksToNextStep = 0;
			currentStep = totalSteps;
		}
	}
}
