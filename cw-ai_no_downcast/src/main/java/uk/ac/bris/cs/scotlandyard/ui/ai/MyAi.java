package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ValueGraph;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import static java.lang.Math.sqrt;


public class MyAi implements Ai {


	@Nonnull @Override public String name() { return "Mone"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		//manage the timeout for mrx to make a move
		long timeoutMillis = TimeUnit.MILLISECONDS.convert(timeoutPair.left(), timeoutPair.right());
		long startTime = System.currentTimeMillis();
		long timeoutAt = startTime + timeoutMillis - 2000;

		//if it doesnt find the best move it defaults to the first move in availablemoves
		Move bestMove = board.getAvailableMoves().iterator().next();
		double highScore = Double.MIN_VALUE;
		List<Integer> detectiveLocations = getDetectiveLocations(board);
		int depth = 2;
		//if theres more than 3 detectives the ai is slow
		if (detectiveLocations.size() > 3) {
			depth = 1;
		}

		//goes through every move
		for (Move move : board.getAvailableMoves()) {

			//if there isnt enough time
			if (System.currentTimeMillis() > timeoutAt) {
				return bestMove;
			}

			Integer mrxLocation = getMoveSource(move);
			Integer mrxMoveLocation = getMoveDestination(move);

			//only double moves if it really needs
			if (score(mrxLocation, board, detectiveLocations) > 2 && getMoveTicket(move).size() == 2){
				continue;
			}

			if (!mrxLocation.equals(mrxMoveLocation)) {
				//create tree
				Node moveNode = new Node(0, true);
				populateTree(moveNode, board, mrxMoveLocation, detectiveLocations, depth);

				MinMaxTree tree = new MinMaxTree(moveNode);
				//gets score of tree
				double moveScore = tree.computeMinMax(moveNode, Double.MIN_VALUE, Double.MAX_VALUE);

				if (moveScore > highScore) {
					highScore = moveScore;
					bestMove = move;
				}
			}
		}

		return bestMove;
	}

	private boolean isVulnerable(List<Integer> detectiveLocations,Board board, Integer neighbour) {
		//if mrx moves into a spot where it can be captured
		boolean isVulnerable = false;
		for (Integer detectiveLocation : detectiveLocations) {
			if (board.getSetup().graph.adjacentNodes(detectiveLocation).contains(neighbour)) {
				isVulnerable = true;
				break;
			}
		}
		return isVulnerable;
	}

	private void populateTree(Node node, Board board, Integer mrxLocation, List<Integer> detectiveLocations, int depth){
		//where mrx is captured
		if (detectiveLocations.contains(mrxLocation)) {
			node.value = Double.MIN_VALUE;
			return;
		}

		//tree is populated
		if (depth == 0){
			node.value = score(mrxLocation, board, detectiveLocations);
			return;
		}

		Board.TicketBoard mrxTickets = board.getPlayerTickets(Piece.MrX.MRX).get();

		if (node.findMax){
			for (Integer neighbour : board.getSetup().graph.adjacentNodes(mrxLocation)){
				if (mrxTickets.getCount(ScotlandYard.Ticket.DOUBLE) > 0){
					for (Integer neighbour2 : board.getSetup().graph.adjacentNodes(neighbour)){
						if (!detectiveLocations.contains(neighbour) && !detectiveLocations.contains(neighbour2)){
							if (isVulnerable(detectiveLocations, board, neighbour2)) {
								continue;
							}
							//for a double move
							Node child = new Node(0, false);
							node.addNode(child);
							populateTree(child, board, neighbour2, detectiveLocations,depth-1);
						}
					}
				}
				if (!detectiveLocations.contains(neighbour)){
					if (isVulnerable(detectiveLocations, board, neighbour)) {
						continue;
					}
					//for a single move
					Node child = new Node(0, false);
					node.addNode(child);
					populateTree(child, board, neighbour, detectiveLocations,depth-1);
				}
			}
		}
		else{
			for (Integer detLocation : detectiveLocations){
				for (Integer neighbour : board.getSetup().graph.adjacentNodes(detLocation)){
					if (!detectiveLocations.contains(neighbour)){
						List<List<Integer>> detectiveMoveCombinations = new ArrayList<>();
						createCombinations(allDetectiveMoves(board, mrxLocation), 0, new ArrayList<>(), detectiveMoveCombinations);
						for (List<Integer> combination : detectiveMoveCombinations){
							//each combination of moves for the detectives
							Node child = new Node(0, true);
							node.addNode(child);
							populateTree(child, board, mrxLocation, combination,depth-1);
						}

					}
				}
			}
		}


	}

	private List<List<Integer>> allDetectiveMoves(Board board, Integer mrxLocation){
		List<Integer> detectiveLocations = getDetectiveLocations(board);
		List<List<Integer>> allDetectiveMoves = new ArrayList<>();
		for (Integer detectiveLocation : detectiveLocations){
			List<Integer> detectiveMoves = new ArrayList<>();
			for (Integer neighbour : board.getSetup().graph.adjacentNodes(detectiveLocation)) {
				if (!detectiveLocations.contains(neighbour)){

					detectiveMoves.add(neighbour);


				}
			}
			//all possible moves for a single detective
			allDetectiveMoves.add(detectiveMoves);
		}
		return allDetectiveMoves;
	}

	private void createCombinations (List<List<Integer>> lists, int index, List<Integer> current, List<List<Integer>> result) {

		if (index == lists.size()) {
			result.add(new ArrayList<>(current));
			return;
		}

		for (Integer item : lists.get(index)) {
			if (current.contains(item)) {
				continue;
			}
			current.add(item);
			createCombinations(lists, index + 1, current, result);
			current.remove(current.size() - 1);
		}
		//all possible combination of moves for the set of detectives
	}




	private List<Integer> getDetectiveLocations(Board board){
		List<Integer> detectiveLocations = new ArrayList<>();

		Piece.Detective[] allDetectives = Piece.Detective.values();

		for (Piece.Detective detective : allDetectives) {

			Optional<Integer> location = board.getDetectiveLocation(detective);

			if (location.isPresent()) {
				Integer locationValue = location.get();
				detectiveLocations.add(locationValue);
			}
		}
		return detectiveLocations;
	}

	private Double score(Integer mrxMoveLocation, Board board, List<Integer>  detectiveLocations) {

		double score = 0;
		double totalDistanceSqrt = 0;
		int freedom = freedomAfterMove(mrxMoveLocation, board, detectiveLocations);

		for (Integer location : detectiveLocations) {
			totalDistanceSqrt += sqrt(findDistance(mrxMoveLocation, location, board));
		}
		//given a score of the sum of square root of all distances


		//if the move is to a location where it cannot be captured
		if (freedom == board.getSetup().graph.adjacentNodes(mrxMoveLocation).size()){
			score += totalDistanceSqrt + (freedom / 25.0);

		}
		else{
			return Double.MIN_VALUE;
		}


		if (isVulnerable(detectiveLocations, board, mrxMoveLocation)) {
			return Double.MIN_VALUE;
		}

		return score;

	}

	private Integer findDistance(int mrxMoveLocation, Integer location, Board board) {
		//breadth first search to find shortest distance
		ValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> newGraph = board.getSetup().graph;
		Map<Integer, Integer> distances = new HashMap<>();
		Queue<Integer> queue = new LinkedList<>();
		Set<Integer> visited = new HashSet<>();

		distances.put(location,0);
		queue.add(location);
		while(!queue.isEmpty()){

			int currentNode = queue.poll();

			if(currentNode == mrxMoveLocation){
				return distances.get(currentNode);
			}

			for(Integer neighbour : newGraph.adjacentNodes(currentNode)){
				if (!visited.contains(neighbour)) {
					visited.add(neighbour);
					distances.put(neighbour, distances.get(currentNode)+1);
					queue.add(neighbour);
				}
			}
		}



		return 1000;
	}

	private Integer freedomAfterMove(int mrxMoveLocation, Board board, List<Integer> detectiveLocations){
		//how many moves mrx can make after the move
		Integer moves = 0;
		Boolean Free;
		for (Integer neighbour : board.getSetup().graph.adjacentNodes(mrxMoveLocation)){
			Free = true;
			for (Integer detLocation : detectiveLocations){
				if (neighbour == detLocation){
					Free = false;
				}
			}
			if(Free){
				moves+=1;
			}
		}

		return moves;
	}

	private Integer getMoveSource(Move move){
		//to get the original location of mrx
		return move.accept(new Move.Visitor<Integer>() {
			@Override
			public Integer visit(Move.SingleMove move) {
				return move.source();
			}

			@Override
			public Integer visit(Move.DoubleMove move) {
				return move.source();
			}
		});
	}

	private Integer getMoveDestination(Move move){
		//get move destination of mrx
		return move.accept(new Move.Visitor<Integer>() {
			@Override
			public Integer visit(Move.SingleMove move) {
				return move.destination;
			}

			@Override
			public Integer visit(Move.DoubleMove move) {
				return move.destination2;
			}
		});
	}
	private List<ScotlandYard.Ticket> getMoveTicket(Move move){
		//get the ticket mrx uses
		return move.accept(new Move.Visitor<List<ScotlandYard.Ticket>>() {
			@Override
			public List<ScotlandYard.Ticket> visit(Move.SingleMove move) {
				List<ScotlandYard.Ticket> tickets = new ArrayList<>();
				tickets.add(move.ticket);
				return tickets;
			}

			@Override
			public List<ScotlandYard.Ticket> visit(Move.DoubleMove move) {
				List<ScotlandYard.Ticket> tickets = new ArrayList<>();
				tickets.add(move.ticket1);
				tickets.add(move.ticket2);
				return tickets;
			}
		});
	}

}
