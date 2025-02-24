package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.Optional;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		// MyGameState class for implementing methods to update the gamestate by returning new gamestate

		@Nonnull
		@Override
		public GameState advance(Move move) {
			// moves will be either single or double move depending on that the travel log has to be updated
			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {
					return null;
				}

				@Override
				public GameState visit(Move.DoubleMove move) {
					return null;
				}
			});
		}

		@Nonnull
		@Override
		public GameSetup getSetup() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			return null;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		throw new RuntimeException("Implement me!");

	}

}
