package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;


import java.util.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		// MyGameState class for implementing methods to update the gamestate by returning new gamestate
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

		}
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
			return setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			return remaining;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
            for (Player player : detectives) {
                if (detective.equals(player.piece())) {
                    return Optional.of(player.location());
                }
            }
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
			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return moves;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		if(setup == null){
			throw new NullPointerException();
		}
		if(setup.moves.isEmpty()) {
			throw new IllegalArgumentException("Moves is empty!");
		}
		if(setup.graph.nodes().isEmpty()){
			throw new IllegalArgumentException();
		}
		if(mrX == null ){
			throw new NullPointerException();
		}
		if(detectives.isEmpty()) {
			throw new NullPointerException();
		}

		Set<Piece> duplicateDetective = new HashSet<>();
		Set<Integer> location = new HashSet<>();
		int mrXCount = 0;

		for(Player player : detectives) {
			if(player.isDetective()){
				if(!duplicateDetective.add(player.piece())){
					throw new IllegalArgumentException();
				}
				if(!location.add(player.location())){
					throw new IllegalArgumentException();
				}
				if(player.has(ScotlandYard.Ticket.SECRET) || player.has(ScotlandYard.Ticket.DOUBLE)){
					throw new IllegalArgumentException();
				}
			}
			if(player.isMrX()){mrXCount +=1;}
		}
		if(!(mrXCount == 0)){
			throw new IllegalArgumentException();
		}

		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);

	}

}
