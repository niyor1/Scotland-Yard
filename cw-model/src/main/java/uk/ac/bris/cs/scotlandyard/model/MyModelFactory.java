package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	private static final class MyModel implements Model {
		private Board.GameState gameState;
		private Set<Observer> observers;
		private boolean gameOver;


		public MyModel(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {

			this.gameState = new MyGameStateFactory().build(setup, mrX, detectives);
			this.observers = new HashSet<>();
			this.gameOver = !gameState.getWinner().isEmpty();

		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return gameState;
		}

		@Override
		public void registerObserver(Observer observer) {
			if(observer == null ){
				throw new NullPointerException();
			}
			if (observers.contains(observer)) {
				throw new IllegalArgumentException("Observer already registered");
			}
			observers.add(observer);

		}

		@Override
		public void unregisterObserver(Observer observer) {
			if(observer == null ){
				throw new NullPointerException();
			}
			if (!observers.contains(observer)) {
				throw new IllegalArgumentException("Observer not registered");
			}
			observers.remove(observer);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			gameState = gameState.advance(move);
			gameOver = !gameState.getWinner().isEmpty();
			if (gameOver) {
				for (Observer observer : observers) {
					observer.onModelChanged(gameState, Observer.Event.GAME_OVER);
				}
			}
			else{
				for (Observer observer : observers) {
					observer.onModelChanged(gameState, Observer.Event.MOVE_MADE);
				}
			}



		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {

		return new MyModel(setup, mrX, detectives);
	}
}
