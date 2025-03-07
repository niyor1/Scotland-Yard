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
			this.moves = ImmutableSet.of();
			this.winner = ImmutableSet.of();

			if (checkGameOver()) {
				this.moves = ImmutableSet.of();
			}
			else{
				getAvailableMoves();
			}

		}
		@Nonnull
		@Override
		public GameState advance(Move move) {
			if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			
			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {

					Set<Move> AllValidSingleMoves = Set.copyOf(getAvailableMoves());
					if (AllValidSingleMoves.contains(move)) {
						if (move.commencedBy().equals(mrX.piece())) {
							Set<Piece> remainingTemp = new HashSet<>(Set.copyOf(remaining));
							List<LogEntry> mrXlog = new ArrayList<>(List.copyOf(getMrXTravelLog()));
							int round = mrXlog.size() + 1;
							mrX = mrX.at(move.destination).use(move.ticket);
							if (setup.moves.get(round - 1)) {
								mrXlog.add(LogEntry.reveal(move.ticket, move.destination));
							} else {
								mrXlog.add(LogEntry.hidden(move.ticket));
							}

							log = ImmutableList.copyOf(mrXlog);


							remainingTemp.remove(mrX.piece());

							for (Player player : detectives) {
								remainingTemp.add(player.piece());
							}
							remaining = ImmutableSet.copyOf(remainingTemp);


						} else {
							for (Player player : detectives) {
								if (move.commencedBy().equals(player.piece())) {
									Set<Piece> remainingTemp = new HashSet<>(Set.copyOf(remaining));
									Set<Player> detectivesTemp = new HashSet<>(Set.copyOf(detectives));

									Player playerAfterMove = player.at(move.destination).use(move.ticket);
									detectivesTemp.remove(player);
									detectivesTemp.add(playerAfterMove);
									detectives = ImmutableList.copyOf(detectivesTemp);


									mrX = mrX.give(move.ticket);

									remainingTemp.remove(player.piece());
									if (remainingTemp.isEmpty()) {
										remainingTemp.add(mrX.piece());
									}
									remaining = ImmutableSet.copyOf(remainingTemp);

								}
							}
						}

					}
					MyGameState newState = new MyGameState(setup, remaining, log, mrX, detectives);
					newState.getAvailableMoves();
					newState.checkGameOver();
					return newState;
                }

				@Override
				public GameState visit(Move.DoubleMove move) {

					Set<Move> AllValidDoubleMoves = Set.copyOf(getAvailableMoves());
					if (AllValidDoubleMoves.contains(move)) {

						Set<Piece> remainingTemp = new HashSet<>(Set.copyOf(remaining));
						remainingTemp.remove(mrX.piece());


						List<LogEntry> mrX2log = new ArrayList<>(List.copyOf(getMrXTravelLog()));
						int round = mrX2log.size() + 1;
						mrX = mrX.at(move.destination2).use(move.ticket1).use(move.ticket2).use(ScotlandYard.Ticket.DOUBLE);
						if (setup.moves.get(round - 1)) {
							mrX2log.add(LogEntry.reveal(move.ticket1, move.destination1));
							if (!setup.moves.get(round)) {
								mrX2log.add(LogEntry.hidden(move.ticket2));
							} else {
								mrX2log.add(LogEntry.reveal(move.ticket2, move.destination2));
							}
						} else {
							mrX2log.add(LogEntry.hidden(move.ticket1));
							if (setup.moves.get(round)) {
								mrX2log.add(LogEntry.reveal(move.ticket2, move.destination2));
							} else {
								mrX2log.add(LogEntry.hidden(move.ticket2));
							}
						}
						log = ImmutableList.copyOf(mrX2log);


						for (Player player : detectives) {
							remainingTemp.add(player.piece());
						}
						remaining = ImmutableSet.copyOf(remainingTemp);

					}
					MyGameState newState = new MyGameState(setup, remaining, log, mrX, detectives);
					newState.getAvailableMoves();
					newState.checkGameOver();
					return newState;
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
			Set<Piece> AllPiecesSet = new HashSet<>();
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

		private Boolean checkGameOver() {
			Set<Piece> winningMrX = new HashSet<>();
			Set<Piece> winningDets = new HashSet<>();

			// check if any detective has captured mrx
			for (Player detective : detectives) {
				if (detective.location() == mrX.location()) {
					for (Player p : detectives) {
						winningDets.add(p.piece());
					}
					winner = ImmutableSet.copyOf(winningDets);
					return true;
				}
			}

			// check if mrx has no valid moves when it's his turn
			if (remaining.contains(mrX.piece())) {
				if (makeSingleMoves(setup, detectives, mrX, mrX.location()).isEmpty() &&
					makeDoubleMoves(setup, detectives, mrX, mrX.location()).isEmpty()) {
					for (Player p : detectives) {
						winningDets.add(p.piece());
					}
					winner = ImmutableSet.copyOf(winningDets);
					return true;
				}
				// check if mrx has used all his moves
				if (log.size() == setup.moves.size()) {
					winningMrX.add(mrX.piece());
					winner = ImmutableSet.copyOf(winningMrX);
					return true;
				}
			}
			else {
				// check if all detectives are stuck when it's their turn
				boolean anyDetectiveCanMove = false;
				for (Player detective : detectives) {
					if (remaining.contains(detective.piece()) &&
						!makeSingleMoves(setup, detectives, detective, detective.location()).isEmpty()) {
						anyDetectiveCanMove = true;
						break;
					}
				}
				if (!anyDetectiveCanMove) {
					winningMrX.add(mrX.piece());
					winner = ImmutableSet.copyOf(winningMrX);
					return true;
				}
			}

			return false;
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
			//hashset to store all available double moves
			Set<Move.DoubleMove> availableMoves = new HashSet<>();
			//if there are no double moves then we empty an empty hash set
			if (setup.moves.size() < 2){
				return availableMoves;
			}

			//if the player isnt mrX or doesnt have a double ticket we return a empty hashset
			if (!player.isMrX() || !player.has(ScotlandYard.Ticket.DOUBLE)) return availableMoves;

			//makes a set with all the first moves mX can makes
			Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			//cycles through each first move that mrX can make
			for (Move.SingleMove firstMove : firstMoves) {
				//applies the changes that mrX would have if he made said move to a temp player
				Player playerAfterFirstMove = player.at(firstMove.destination).use(firstMove.ticket);
				//a new set with all the second moves that mrX could make from the given first moves
				Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, playerAfterFirstMove, firstMove.destination);

				//for each second move we add it to the set
				for (Move.SingleMove secondMove : secondMoves) {
					availableMoves.add(new Move.DoubleMove(player.piece(), source, firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
				}
			}

			//return the set
			return availableMoves;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			//hashset to store all moves that are possible in the given gamestate
			Set<Move> allMoves = new HashSet<>();
			//if remaining contains mrX
			if (!winner.isEmpty()) {
				moves = ImmutableSet.of();
				return moves;
			}
			if (remaining.contains(Piece.MrX.MRX)) {
				//adds both single and double moves to the hashset
				allMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				allMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
			}
			else {
				//loops through each detective
				for (Player detective : detectives) {
					//if remaining contains said detective
					if (remaining.contains(detective.piece())) {
						//add all that detectives single move to the hasset
						allMoves.addAll(makeSingleMoves(setup, detectives, detective, detective.location()));
					}
				}
			}
			//this.moves is now all available moves and we return it
			if (this.moves.isEmpty()){
				checkGameOver();
			}
			this.moves = ImmutableSet.copyOf(allMoves);
			return this.moves;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		//null checks
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
