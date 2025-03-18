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


		Double highScore = 0.0d;


		for (Move move : board.getAvailableMoves()){

			Integer mrxLocation = getMoveSource(move);
			Integer mrxMoveLocation = getMoveDestination(move);
			System.out.println(mrxLocation+"  "+mrxMoveLocation);

			Double moveScore = score(move, board);

			if (mrxLocation == mrxMoveLocation){
				moveScore = 0.0d;
			}

			if (highScore < moveScore){
				highScore = moveScore;
				bestMove = move;

			}
		}

		return bestMove;
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

	private Double score(Move move, Board board) {

		Integer mrxMoveLocation = getMoveDestination(move);

		List<Integer> detectiveLocations = getDetectiveLocations(board);
		List<Integer> distanceToMrX = new ArrayList<>();

		for (Integer location : detectiveLocations) {
			distanceToMrX.add(findDistance(mrxMoveLocation, location, board));
		}

		double score = 0;
		for (Integer i : distanceToMrX) {
			score += sqrt(i);
		}

		if (freedomAfterMove(mrxMoveLocation, board, detectiveLocations) != board.getSetup().graph.adjacentNodes(mrxMoveLocation).size()){
			score = Double.MIN_VALUE;
		}
		else{
			score = score + (freedomAfterMove(mrxMoveLocation, board, detectiveLocations) / 15);
		}
		System.out.println(board.getSetup().graph.adjacentNodes(mrxMoveLocation).size());
		System.out.println(freedomAfterMove(mrxMoveLocation, board, detectiveLocations));
		System.out.println();
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

			for(Integer neigbour : newGraph.adjacentNodes(currentNode)){
				if (!visited.contains(neigbour)) {
					visited.add(neigbour);
					distances.put(neigbour, distances.get(currentNode)+1);
					queue.add(neigbour);
				}
			}
		}



		return 0;
	}

	private Integer freedomAfterMove(int mrxMoveLocation, Board board, List<Integer> detectiveLocations){
		Integer moves = 0;
		Boolean Free = true;
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
