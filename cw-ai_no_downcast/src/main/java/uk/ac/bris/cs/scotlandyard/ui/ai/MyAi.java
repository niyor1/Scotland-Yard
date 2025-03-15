package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ValueGraph;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;


public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Mone"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {

		Move bestMove = null;
		Double highScore = 0.0d;
		for (Move move : board.getAvailableMoves()){
			Double moveScore = score(move, board);
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

		final int mrxMoveLocation = move.accept(new Move.Visitor<Integer>() {
			@Override
			public Integer visit(Move.SingleMove move) {
				return move.destination;
			}

			@Override
			public Integer visit(Move.DoubleMove move) {
				return move.destination2;
			}
		});

		List<Integer> detectiveLocations = getDetectiveLocations(board);
		List<Integer> distanceToMrX = new ArrayList<>();
		for (Integer location : detectiveLocations) {
			distanceToMrX.add(findDistance(mrxMoveLocation, location, board));
		}

		return 0.0d;

	}

	private Integer findDistance(int mrxMoveLocation, Integer location, Board board) {
		ValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> newGraph = board.getSetup().graph;

		return 0;
	}

}
