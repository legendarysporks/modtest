package com.example.examplemod.thorhammer;

import com.example.examplemod.utilities.commands.Setting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class SnakeEffect {
	public static final String COMMAND_NAME = "SnakeEffect";
	public static final String COMMAND_USAGE = "Try /SnakeEffect settings";
	public static final String[] COMMAND_ALIASES = {};
	public static final String CONFIG_VERSION = "0.1";

	private final List<BlockGunAffect> activeAffects = new ArrayList<>();
	private double crashRange = 30;
	private int crashAffectDurationInTicks = 3 * 20; // 10 seconds
	private int crashTrailLength = 15;
	private long crashStepDurationInTicks = Math.round(crashAffectDurationInTicks / crashRange);
	private SnakeEffectImpl impl;

	public SnakeEffect(SnakeEffectImpl impl) {
		this.impl = impl;
	}

	//----------------------------------------------------------------------------------------
	// subclass interface

	//   (subclass calls startAffect) - this is what starts things
	//   handleStartPosition - you get a chance to do something at the starting block
	//   while not at end position
	//     handleRemovePosition - cleanup previously added positions that cause the list of
	//             visitedLocations to grow to be greater than crashTrailLength
	//     calculateNextPosition - where are we moving next
	//     handleAddPosition - add next position along path to end position and do what we
	//             want to that location
	//   endwhile
	//   handleRemovePosition - cleanup any positions that haven't been cleaned up yet
	//   handleFinishPosition

	/** clients should call this to start the snake affects */
	public void startAffect(World world, BlockPos start, BlockPos finish) {
		if (activeAffects.isEmpty()) {
			MinecraftForge.EVENT_BUS.register(this);
		}
		activeAffects.add(new BlockGunAffect(world, start, finish));
	}

	public interface SnakeEffectImpl {
		/** Called once when the affect starts */
		default void handleStartPosition(World world, BlockPos pos) {
		}

		/** Return the next position or null if done affecting new locations */
		default Vec3d calculateNextPosition(World world, Collection<BlockPos> prevPos, Vec3d lastPos, Vec3d stepSize) {
			return lastPos.add(stepSize);
		}

		/** add initial affect to location.  Return true if the affect should continue going */
		default void handleAddPosition(World world, BlockPos pos) {
		}

		/** cleanup affect previously at the given location */
		default void handleRemovePosition(World world, BlockPos pos) {
		}

		/** add final affect to the location */
		default void handleFinishPosition(World world, BlockPos pos) {
		}
	}

	//----------------------------------------------------------------------------------------
	// Settings

	@Setting
	public double getCrashRange() {
		return crashRange;
	}

	@Setting
	public void setCrashRange(double crashRange) {
		this.crashRange = crashRange;
		crashStepDurationInTicks = Math.round(crashAffectDurationInTicks / crashRange);
	}

	@Setting
	public int getCrashDurationTicks() {
		return crashAffectDurationInTicks;
	}

	@Setting
	public void setCrashDurationTicks(int affectDurationInTicks) {
		this.crashAffectDurationInTicks = affectDurationInTicks;
		crashStepDurationInTicks = Math.round(affectDurationInTicks / crashRange);
	}

	@Setting
	public double getCrashVelocity() {
		return crashRange / crashAffectDurationInTicks;
	}

	@Setting
	public void setCrashVelocity(double velocity) {
		setCrashDurationTicks((int) Math.round(crashRange / velocity));
	}

	@Setting
	public int getCrashTrailLength() {
		return crashTrailLength;
	}

	@Setting
	public void setCrashTrailLength(int crashTrailLength) {
		this.crashTrailLength = crashTrailLength;
	}

	//----------------------------------------------------------------------------------------
	// utility functions

	public static BlockPos toBlockPos(Vec3d v) {
		return new BlockPos(v.x, v.y, v.z);
	}

	public static Vec3d toVec3d(BlockPos p) {
		return new Vec3d(p.getX(), p.getY(), p.getZ());
	}

	//----------------------------------------------------------------------------------------
	// animation routines

	/** tick.  Deal with affect */
	@SubscribeEvent
	public void handleTickEvents(TickEvent.ServerTickEvent event) {
		List<BlockGunAffect> finishedAffects = new ArrayList<>();
		for (BlockGunAffect affect : activeAffects) {
			if (!affect.doStep()) {
				finishedAffects.add(affect);
			}
		}
		activeAffects.removeAll(finishedAffects);
		if (activeAffects.isEmpty()) {
			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}

	/**
	 * A BlockGunAffect is created whenever a ThorHammer is used to break a block.  Each tracks
	 * a dynamic affect over time that takes place between a point of origin and a destination point.
	 */
	private class BlockGunAffect {
		private World world;
		private int totalSteps;
		private int currentStep;
		private Vec3d currentPos;
		private Vec3d stepSize;
		private Queue<BlockPos> positionsVisited = new ArrayDeque<>(crashTrailLength);
		private long ticksToNextStep = 0;

		public BlockGunAffect(World world, BlockPos start, BlockPos finish) {
			this.world = world;
			int dx = (finish.getX() - start.getX());
			int dy = (finish.getY() - start.getY());
			int dz = (finish.getZ() - start.getZ());
			// We want to step one pixel at a time along the longest axis
			totalSteps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
			stepSize = new Vec3d((double) dx / totalSteps, (double) dy / totalSteps, (double) dz / totalSteps);
			currentPos = toVec3d(start);
			currentStep = 0;
			impl.handleStartPosition(world, start);
		}

		public boolean doStep() {
			// terminate the whole thing is we've done all our steps & undone all our stuff
			if (currentStep > totalSteps && positionsVisited.isEmpty()) return false;

			// if we haven't waited long enough to do the next step, just exit
			if (ticksToNextStep-- > 0) return true;

			// An entire crashStepDurationInTicks has elapsed, so reset timer and do stuff
			ticksToNextStep = crashStepDurationInTicks;

			if (positionsVisited.size() >= crashTrailLength) {
				// We've reached the max trail size, remove an old pixel so we can add a new one.
				impl.handleRemovePosition(world, positionsVisited.remove());
			}

			if (currentStep < totalSteps) {
				// We haven't reached the finish yet, add a location
				Vec3d nextPos = impl.calculateNextPosition(world, positionsVisited, currentPos, stepSize);
				if (nextPos == null) {
					// there is no next position, so end things gracefully.
					end();
				} else {
					currentPos = nextPos;
					BlockPos currentBlockPos = toBlockPos(currentPos);
					currentStep = currentStep + 1;

					// if this position hasn't been touched yet (which might happen due to round off errors, twiddle the block
					if (!positionsVisited.contains(currentBlockPos)) {
						// add a new position to the list and handle it
						positionsVisited.add(currentBlockPos);
						impl.handleAddPosition(world, currentBlockPos);
					}
				}
				return true;
			} else if (!positionsVisited.isEmpty()) {
				// we've reached the end so now we just remove positionsVisited along the line 1 by 1
				// until they are all gone
				impl.handleRemovePosition(world, positionsVisited.remove());
				return true;
			} else {
				// We've reached the finish and we've removed all positionsVisited remaining.   Done.
				impl.handleFinishPosition(world, toBlockPos(currentPos));
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
