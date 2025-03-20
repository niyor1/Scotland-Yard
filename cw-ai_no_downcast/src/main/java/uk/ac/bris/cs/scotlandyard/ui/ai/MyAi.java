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

		long timeoutMillis = TimeUnit.MILLISECONDS.convert(timeoutPair.left(), timeoutPair.right());
		long startTime = System.currentTimeMillis();
		long timeoutAt = startTime + timeoutMillis - 2000;

		Move bestMove = board.getAvailableMoves().iterator().next();
		double highScore = Double.MIN_VALUE;
		List<Integer> detectiveLocations = getDetectiveLocations(board);
		List<ScotlandYard.Ticket> ticketsUsed = new ArrayList<>();

		for (Move move : board.getAvailableMoves()) {
			if (System.currentTimeMillis() > timeoutAt) {
				return bestMove;
			}

			Integer mrxLocation = getMoveSource(move);
			Integer mrxMoveLocation = getMoveDestination(move);
			ticketsUsed.addAll(getMoveTicket(move));

			if (!mrxLocation.equals(mrxMoveLocation)) {
				Node moveNode = new Node(0, true);
				populateTree(moveNode, board, mrxMoveLocation, detectiveLocations, 2, ticketsUsed);

				MinMaxTree tree = new MinMaxTree(moveNode);
				double moveScore = tree.computeMinMax(moveNode, Double.MIN_VALUE, Double.MAX_VALUE);

				if (highScore < moveScore) {
					highScore = moveScore;
					bestMove = move;
				}
			}
		}

		return bestMove;
	}

	private void populateTree(Node node, Board board, Integer mrxLocation, List<Integer> detectiveLocations, int depth,List<ScotlandYard.Ticket> ticketsUsed){
		if (detectiveLocations.contains(mrxLocation)) {
			node.value = Double.MIN_VALUE;
			return;
		}

		if (depth == 0){
			node.value = score(mrxLocation, board, detectiveLocations,ticketsUsed);
			return;
		}

		Board.TicketBoard mrxTickets = board.getPlayerTickets(Piece.MrX.MRX).get();

		if (node.findMax){
			for (Integer neighbour : board.getSetup().graph.adjacentNodes(mrxLocation)){
				if (mrxTickets.getCount(ScotlandYard.Ticket.DOUBLE) > 0){
					for (Integer neighbour2 : board.getSetup().graph.adjacentNodes(neighbour)){
						if (!detectiveLocations.contains(neighbour) && !detectiveLocations.contains(neighbour2) && (score(neighbour2, board, detectiveLocations, ticketsUsed) < 2)){
							Node child = new Node(0, false);
							node.addNode(child);
							populateTree(child, board, neighbour2, detectiveLocations,depth-1,ticketsUsed);
						}
					}
				}
				if (!detectiveLocations.contains(neighbour)){
					Node child = new Node(0, false);
					node.addNode(child);
					populateTree(child, board, neighbour, detectiveLocations,depth-1,ticketsUsed);
				}
			}
		}
		else{
			for (Integer detLocation : detectiveLocations){
				for (Integer neighbour : board.getSetup().graph.adjacentNodes(detLocation)){
					if (!detectiveLocations.contains(neighbour)){
						List<List<Integer>> detectiveMoveCombinations = new ArrayList<>();
						createCombinations(allDetectiveMoves(board), 0, new ArrayList<>(), detectiveMoveCombinations);
						System.out.println(detectiveMoveCombinations);
						for (List<Integer> combination : detectiveMoveCombinations){
							Node child = new Node(0, true);
							node.addNode(child);
							populateTree(child, board, mrxLocation, combination,depth-1,ticketsUsed);
						}

					}
				}
			}
		}


	}

	private List<List<Integer>> allDetectiveMoves(Board board){
		List<Integer> detectiveLocations = getDetectiveLocations(board);
		List<List<Integer>> allDetectiveMoves = new ArrayList<>();
		for (Integer detectiveLocation : detectiveLocations){
			List<Integer> detectiveMoves = new ArrayList<>();
			for (Integer neighbour : board.getSetup().graph.adjacentNodes(detectiveLocation)) {
				if (!detectiveLocations.contains(neighbour)){
					detectiveMoves.add(neighbour);
				}
			}
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

	private Double score(Integer mrxMoveLocation, Board board, List<Integer>  detectiveLocations, List<ScotlandYard.Ticket> ticketsUsed) {

		Board.TicketBoard mrXtickets = board.getPlayerTickets(Piece.MrX.MRX).get();
		List<Integer> distanceToMrX = new ArrayList<>();
		double score = 0;
		double totalDistanceSqrt = 0;
		int freedom = freedomAfterMove(mrxMoveLocation, board, detectiveLocations);

		for (Integer location : detectiveLocations) {
			distanceToMrX.add(findDistance(mrxMoveLocation, location, board));
		}

		for (Integer i : distanceToMrX) {
			totalDistanceSqrt += sqrt(i);
		}

		if (freedom == board.getSetup().graph.adjacentNodes(mrxMoveLocation).size()){
			score += totalDistanceSqrt + (freedom / 20.0);

		}
		else{
			return Double.MIN_VALUE;
		}

		if ((mrXtickets.getCount(ticketsUsed.get(0))) < 3){
			score /= 2;
		}

		System.out.println(score);
		return score;

	}

	private Integer findDistance(int mrxMoveLocation, Integer location, Board board) {
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
