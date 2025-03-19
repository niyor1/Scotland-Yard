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
			this.winner = getWinner();
			this.moves = getAvailableMoves();
		}
		@Nonnull
		@Override
		public GameState advance(Move move) {
			//if the log size is bigger than the number of moves then the game is over and you return nothing
			if (log.size() >= setup.moves.size()) {
				return this;
			}
			//if moves doesnt contain the move chosen then it will throw an error
			if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);


			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {
					//Copy set of the remaining players to move
                    Set<Piece> remainingTemp = new HashSet<>(Set.copyOf(remaining));
					//if the move has been made by Mr X
                    if (move.commencedBy().equals(mrX.piece())) {
						//Copy of the log entries
                        List<LogEntry> mrXlog = new ArrayList<>(List.copyOf(getMrXTravelLog()));
						//integer variable to know what round it is
						int round = mrXlog.size() + 1;
						//we complete the move and move mrX location and use the ticket
						mrX = mrX.at(move.destination).use(move.ticket);
						//if it is a reveal round then you add a reveal log entry with both destination and ticket
						if (setup.moves.get(round - 1)) {
							mrXlog.add(LogEntry.reveal(move.ticket, move.destination));
						}
						//otherwise its a hide round and you only show the ticket
						else {
							mrXlog.add(LogEntry.hidden(move.ticket));
						}
						//now update the log of the class
						log = ImmutableList.copyOf(mrXlog);
						//remove mrx from remaining as he has completed his move now
						remainingTemp.remove(mrX.piece());
						//for each detective if the moves for each detective isnt empty then you add that detective to remaining
						for (Player player : detectives) {
							if (!makeSingleMoves(setup, detectives, player, player.location()).isEmpty()) {
								remainingTemp.add(player.piece());
							}
						}
						//updates the remaining of the class
						remaining = ImmutableSet.copyOf(remainingTemp);


					}
					//otherwise its a move made by a detective
					else {
						//make a temp of all the detectives Set
                        Set<Player> detectivesTemp = new HashSet<>(Set.copyOf(detectives));

						//for each player in detective
						for (Player player : detectives) {
							//if the detective is the one who made the move
							if (move.commencedBy().equals(player.piece())) {
								//make a copy player for the player after the move
								Player playerAfterMove = player.at(move.destination).use(move.ticket);
								//remove the original player from detectives and add the new one
								detectivesTemp.remove(player);
								detectivesTemp.add(playerAfterMove);
								//change the detectives of the class
								detectives = ImmutableList.copyOf(detectivesTemp);

								//give mrX the ticket that we jsut used
								mrX = mrX.give(move.ticket);

								//remove the player from temp
								remainingTemp.remove(player.piece());
								//if temp is empty then add mrX
								if (remainingTemp.isEmpty()) {
									remainingTemp.add(mrX.piece());
								}

							}
						}
						//change the remaining of the class
						remaining = ImmutableSet.copyOf(remainingTemp);
					}


					//now return the new game state
                    return new MyGameState(setup, remaining, log, mrX, detectives);
                }

				@Override
				public GameState visit(Move.DoubleMove move) {
					//if its a double move
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
						if (!makeSingleMoves(setup, detectives, player, player.location()).isEmpty()) {
							remainingTemp.add(player.piece());
						}
					}
					remaining = ImmutableSet.copyOf(remainingTemp);


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


		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			//returns the winner
			Set<Piece> theWinner = new HashSet<>();

			//check if any detective has captured mrx
			for (Player detective : detectives) {
				if (detective.location() == mrX.location()) {
					for (Player p : detectives) {
						theWinner.add(p.piece());
					}
					return ImmutableSet.copyOf(theWinner);
				}
			}

			//check if mrx has no valid moves when it's his turn
			if (remaining.contains(mrX.piece())) {
				Set<Move> mrXMoves = new HashSet<>();
				mrXMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				mrXMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));

				if (mrXMoves.isEmpty()) {
					for (Player p : detectives) {
						theWinner.add(p.piece());
					}
					return ImmutableSet.copyOf(theWinner);
				}
			}

			boolean allDetectivesStuck = true;
			for (Player detective : detectives) {
				if (!makeSingleMoves(setup, detectives, detective, detective.location()).isEmpty()) {
					allDetectivesStuck = false;
					break;
				}
			}

			//if all detectives are stuck, mrX wins
			if (allDetectivesStuck && !remaining.isEmpty() && !remaining.contains(mrX.piece())) {
				theWinner.add(mrX.piece());
				return ImmutableSet.copyOf(theWinner);
			}

			//check if detectives have no tickets
			boolean allDetectivesOutOfTickets = true;
			for (Player detective : detectives) {
				int totalTickets = 0;
				for (Integer count : detective.tickets().values()) {
					totalTickets += count;
				}
				if (totalTickets > 0) {
					allDetectivesOutOfTickets = false;
					break;
				}
			}

			if (allDetectivesOutOfTickets) {
				theWinner.add(mrX.piece());
				return ImmutableSet.copyOf(theWinner);
			}

			//check mrx has used all his moves
			if (log.size() >= setup.moves.size()) {
				theWinner.add(mrX.piece());
				return ImmutableSet.copyOf(theWinner);
			}

			return ImmutableSet.copyOf(theWinner);
		}


		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			//makes a new hash list to store all available moves
			Set<Move.SingleMove> availableMoves = new HashSet<>();
			//loop through each adjacent node
			for(int destination : setup.graph.adjacentNodes(source)) {
				//set a boolean that is false if not occupied by a detective
				boolean Occupied = false;
				//loop through each detective
				for (Player detective : detectives){
					//if there is a detective then set boolean to true then break
                    if (detective.location() == destination) {
                        Occupied = true;
                        break;
                    }
				}
				//if its not occupied then we can process
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
			if (!winner.isEmpty()) {
				return ImmutableSet.of();
			}
			//if remaining contains mrX
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
						//add all that detectives single move to the hashset
						allMoves.addAll(makeSingleMoves(setup, detectives, detective, detective.location()));
					}
				}
			}
			//this.moves is now all available moves and we return it
			return ImmutableSet.copyOf(allMoves);
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
