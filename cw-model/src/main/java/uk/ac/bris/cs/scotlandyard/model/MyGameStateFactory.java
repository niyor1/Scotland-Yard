package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.swing.text.html.Option;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.N;
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
			this.winner = ImmutableSet.of();
			this.moves = ImmutableSet.of();

		}
		@Nonnull
		@Override
		public GameState advance(Move move) {
			getAvailableMoves();
			if(!(moves.contains(move))) throw new IllegalArgumentException("Illegal move: "+move);
			// moves will be either single or double move depending on that the travel log has to be updated
			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {
					Set<Move> AllValidSingleMoves= Set.copyOf(getAvailableMoves());
					//if(!(AllValidSingleMoves.contains(move))) throw new IllegalArgumentException("Illegal move: "+move);
					if(AllValidSingleMoves.contains(move)){
						if(move.commencedBy().equals(mrX.piece())){
							List<LogEntry> mrXlog = List.copyOf(getMrXTravelLog());
							Set<Piece> remainingTemp = new HashSet<>(Set.copyOf(remaining));
							mrX.at(move.destination).use(move.ticket);
							remainingTemp.remove(mrX.piece());
							for (Player player : detectives) {
								remainingTemp.add(player.piece());
							}
							remaining = ImmutableSet.copyOf(remainingTemp);
							System.out.println(move);
							System.out.println(remaining);



						}
						else {
							for (Player player : detectives) {
								if (move.commencedBy().equals(player.piece())) {
									Set<Piece> remainingTemp = new HashSet<>(Set.copyOf(remaining));
									remainingTemp.remove(player.piece());
									player.at(move.destination).use(move.ticket);
									if (remaining.isEmpty()){
										remainingTemp.add(mrX.piece());
									}
									remaining = ImmutableSet.copyOf(remainingTemp);
									if (player.location() == mrX.location()) {
										Set<Piece> winningDets = new HashSet<>();
										for (Player p : detectives) {
											winningDets.add(p.piece());
										}
										winner = ImmutableSet.copyOf(winningDets);
									}
								}
							}
						}

					}
					//else{
					//	throw new IllegalArgumentException("Illegal move: "+move);
					//}
					//update log
					//update move
					//see if move
					return new MyGameState(setup,remaining,log, mrX, detectives);
				}

				@Override
				public GameState visit(Move.DoubleMove move) {
					Set<Move> AllValidDoubleMoves = Set.copyOf(getAvailableMoves());
					if (AllValidDoubleMoves.contains(move)) {
						List<LogEntry> mrXlog = List.copyOf(getMrXTravelLog());
						Set<Piece> remainingTemp = new HashSet<>(Set.copyOf(remaining));
						remainingTemp.remove(mrX.piece());
						mrX.at(move.destination2).use(move.ticket1).use(move.ticket2);
						for (Player player : detectives) {
							remainingTemp.add(player.piece());
						}
						remaining = ImmutableSet.copyOf(remainingTemp);
						System.out.println(move);

					}
					return new MyGameState(setup, remaining, log, mrX, detectives);
				}
			});
		}

		@Nonnull
		@Override
		public GameSetup getSetup() {
			//returns the setup
			return setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			//creates a new hashset that will contain all the pieces
			Set<Piece> AllPiecesSet = new HashSet<Piece>();
			//for each player in detective, add their piece to the hash set
			for (Player player : detectives) {
				AllPiecesSet.add(player.piece());
			}
			//add mrX's piece to the hashset
			AllPiecesSet.add(mrX.piece());
			return ImmutableSet.copyOf(AllPiecesSet);
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			//go through the list of detectives
			//if the piece matches one in detective
            for (Player player : detectives) {
                if (detective.equals(player.piece())) {
					//returns location if there is one
                    return Optional.of(player.location());
                }
            }
			//returns empty otherwise
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			//creates a new ticketboard
			TicketBoard playerTicks = new TicketBoard() {
				@Override
				//implements the getCount method
				public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
					//if its one of the detectives then we return their tickets
					for (Player player : detectives) {
						if (piece.equals(player.piece())) {
							return player.tickets().get(ticket);
						}
					}
					//if its mrX, then we return his tickets
					if (piece.equals(mrX.piece())) {
						return mrX.tickets().get(ticket);
					}
					//if they dont exist we return 0
					return 0;
				}
			};
			for (Player player : detectives) {
				if (piece.equals(player.piece())) {
					return Optional.of(playerTicks);
				}
			}
			if (piece.equals(mrX.piece())) {
				return Optional.of(playerTicks);
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			//returns the log of mrX
			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			//returns the winner
			return winner;
		}


		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			//makes a new hash list to store all available moves
			Set<Move.SingleMove> availableMoves = new HashSet<>();
			//loop through each adjacent node
			for(int destination : setup.graph.adjacentNodes(source)) {
				//set a boolean that is false if not occcupied by a detective
				boolean Occupied = false;
				//loop through each detective
				for (Player detective : detectives){
					//if there is a detective then set boolean to true then break
                    if (detective.location() == destination) {
                        Occupied = true;
                        break;
                    }
				}
				//if its not occupied then we can procees
				if (!Occupied) {
					//loop through each transport type from source node to destination node
					for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
						//if the player has the required ticket then we add it to the hashset
						if (player.has(t.requiredTicket())) {
							availableMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}
					}
					//if the player is mr x and has a secret ticket then we can add their move regardless
					if (player.isMrX() && player.has(ScotlandYard.Ticket.SECRET)) {
						availableMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
					}
				}
			}
			//return the hashset at the end
			return availableMoves;
		}

		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			Set<Move.DoubleMove> availableMoves = new HashSet<>();
			if (setup.moves.size() < 2){
				return availableMoves;
			}

			if (!player.isMrX() || !player.has(ScotlandYard.Ticket.DOUBLE)) return availableMoves;

			Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);
			
			for (Move.SingleMove firstMove : firstMoves) {
				Player playerAfterFirstMove = player.at(firstMove.destination).use(firstMove.ticket);
				Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, playerAfterFirstMove, firstMove.destination);
				
				for (Move.SingleMove secondMove : secondMoves) {
					availableMoves.add(new Move.DoubleMove(player.piece(), source, firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
				}
			}

			return availableMoves;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> allMoves = new HashSet<>();
			if (remaining.contains(Piece.MrX.MRX)) {
				allMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				allMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
			}
			else {
				for (Player detective : detectives) {
					if (remaining.contains(detective.piece())) {
						allMoves.addAll(makeSingleMoves(setup, detectives, detective, detective.location()));
					}
				}
			}

			this.moves = ImmutableSet.copyOf(allMoves);
			return this.moves;
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
			throw new IllegalArgumentException();
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
