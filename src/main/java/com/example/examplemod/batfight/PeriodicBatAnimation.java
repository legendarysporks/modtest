package com.example.examplemod.batfight;

/*
An interface where a frame only changes once per a given time interval.
The doStuff(...) method is called repeatedly whenever the screen is
being rendered.  This will most likely occur more frequently than
an animation changes frames.
There are two types of animation this class supports
1 - play every frame but allow uneven frame timing.  For this clients should
use the frameIndex to determine which frame to draw.  Every frame will be
displayed at least frameDelayMillis.
2 - animation strictly synced to real time (ex. animation with sound).
Clients should calculate what to display primarly based on elapsedTime
which contains the elapsed time since the animation started.  It may also
want to use timeSinceLastFrame to decide if it wants to skip frames.
 */
public class PeriodicBatAnimation implements BatAnimation {
	private long firstFrameTime = 0;
	// system time in ms the last time the frame was drawn
	private long frameStartTime = 0;
	// the current frame number
	private int frameIndex = -1;
	// how frequently the frame should advance
	private long frameDelayMillis = 1000;

	public PeriodicBatAnimation() {
	}

	public PeriodicBatAnimation(long frameDelayMillis) {
		this.frameDelayMillis = frameDelayMillis;
	}

	@Override
	public boolean doStuff() {
		long now = System.currentTimeMillis();
		if (frameIndex == -1) {
			// this is the very first frame
			firstFrameTime = now;
			frameStartTime = now;
			frameIndex = 0;
			return doStuff(0, 0, 0, true);
		} else {
			// for frames other than the first
			long elapsedTime = now - firstFrameTime;
			long timeSinceLastFrame = now - frameStartTime;
			boolean frameChange = timeSinceLastFrame > frameDelayMillis;

			if (frameChange) {
				frameIndex = frameIndex + 1;
				frameStartTime = now;
			}
			return doStuff(elapsedTime, timeSinceLastFrame, frameIndex, frameChange);
		}
	}

	protected boolean doStuff(long elapsedTime, long timeSinceLastFrame, int frameIndex, boolean newFrame) {
		return true;
	}
}
