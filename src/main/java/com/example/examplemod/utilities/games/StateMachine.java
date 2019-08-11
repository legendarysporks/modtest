package com.example.examplemod.utilities.games;

public class StateMachine {
	public interface State {
		State execute();
	}

	/*
	gatherGame -> gatherGame | equipGame | gameFailed
	beginGame  -> goalsNotMet
	goalsNotMet -> goalsNotMet | declareWinner | gameFailed
	declareWinner -> endGame
	gameFailed -> endGame
	 */

	public void runGame(State initialState) {
		State state = initialState;
		do {
			state = state.execute();
		} while (state != null);
	}
}

