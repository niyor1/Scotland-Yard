package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		// TODO
		return new Model() {
			@Nonnull
			//observers in here set of
			//use builder - get gamestate
			@Override
			public Board getCurrentBoard() {
				return null;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {

			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {

			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return null;
			}

			@Override
			public void chooseMove(@Nonnull Move move) {
				//long
				//adance the game state
				//update all the observers that theres either a winner or a move has been made
				//Event.
				//iterate over observers

			}
		};
	}
}
