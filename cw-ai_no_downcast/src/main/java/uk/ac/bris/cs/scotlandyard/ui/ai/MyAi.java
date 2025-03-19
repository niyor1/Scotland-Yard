package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.swing.*;

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

		Move bestMove = null;
		double highScore = Double.MIN_VALUE;
		List<Integer> detectiveLocations = getDetectiveLocations(board);

		for (Move move : board.getAvailableMoves()){

			Integer mrxLocation = getMoveSource(move);
			Integer mrxMoveLocation = getMoveDestination(move);

			if (!mrxLocation.equals(mrxMoveLocation)){
				Node moveNode = new Node(0, true);
				populateTree(moveNode, board, mrxMoveLocation, detectiveLocations,2);

				MinMaxTree tree = new MinMaxTree(moveNode);
				double moveScore = tree.computeMinMax(moveNode);

				if (highScore < moveScore){
					highScore = moveScore;
					bestMove = move;

				}
			}


		}


		return bestMove;
	}

	private void populateTree(Node node, Board board, Integer mrxLocation, List<Integer> detectiveLocations, int depth){
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
							Node child = new Node(0, false);
							node.addNode(child);
							populateTree(child, board, neighbour2, detectiveLocations,depth-1);
						}
					}
				}
				if (!detectiveLocations.contains(neighbour)){
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
						createCombinations(allDetectiveMoves(board), 0, new ArrayList<>(), detectiveMoveCombinations);
						System.out.println(detectiveMoveCombinations);
						for (List<Integer> combination : detectiveMoveCombinations){
							Node child = new Node(0, true);
							node.addNode(child);
							populateTree(child, board, mrxLocation, combination,depth-1);
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

	private Double score(Integer mrxMoveLocation, Board board, List<Integer>  detectiveLocations) {

		List<Integer> distanceToMrX = new ArrayList<>();

		for (Integer location : detectiveLocations) {
			distanceToMrX.add(findDistance(mrxMoveLocation, location, board));
		}

		double score = 0;
		for (Integer i : distanceToMrX) {
			score += sqrt(i);
		}
		int freedom = freedomAfterMove(mrxMoveLocation, board, detectiveLocations);

		if (freedom != board.getSetup().graph.adjacentNodes(mrxMoveLocation).size()){
			return Double.MIN_VALUE;
		}

		return score + (freedom / 15);

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

}
