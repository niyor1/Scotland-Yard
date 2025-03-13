package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Mone"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		List <Piece.Detective> detectives = new ArrayList<>();
		for (Piece piece: board.getPlayers()){
			if (piece != Piece.MrX.MRX){
				Piece.Detective detective = (Piece.Detective) piece;
				detectives.add(detective);
			}
		}
		List <Optional<Integer>> detectiveLocations = new ArrayList<>();
		for (Piece.Detective detective: detectives){
			detectiveLocations.add(board.getDetectiveLocation(detective));
		}
		System.out.println(detectiveLocations);
		for (Move move : board.getAvailableMoves()){
			Board newBoard = board;

		}
		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
	}
}
