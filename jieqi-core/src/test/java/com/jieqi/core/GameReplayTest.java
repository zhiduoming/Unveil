package com.jieqi.core;

import com.jieqi.record.ReplayTimeline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameReplayTest {

    @Test
    void processMove_growsReplayTimeline() {
        Game game = new Game("test_replay");
        game.setStatus(Game.GameStatus.PLAYING);
        game.recordReplayInitialIfNeeded();
        assertEquals(1, game.getReplayTimeline().size());

        Board board = game.getBoard();
        // 红方首步合法着
        Move move = findFirstLegalMove(game);
        assertNotNull(move);
        String err = game.processMove(move, ChessPiece.RED);
        assertNull(err);
        assertEquals(2, game.getReplayTimeline().size());
        assertNotNull(game.getReplayTimeline().getFrame(1).getMove());
    }

    @Test
    void replayFrames_areIndependentCopies() {
        Game game = new Game("test_copy");
        game.setStatus(Game.GameStatus.PLAYING);
        game.recordReplayInitialIfNeeded();
        Board snapshot = game.getReplayTimeline().getFrame(0).getBoardSnapshot();
        game.getBoard().clearAllPieces();
        assertNotNull(snapshot.getPiece(9, 4));
    }

    private static Move findFirstLegalMove(Game game) {
        Board board = game.getBoard();
        int color = ChessPiece.RED;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null || p.getColor() != color) {
                    continue;
                }
                        String from = Coordinate.format(r, c);
                        for (int tr = 0; tr < 10; tr++) {
                            for (int tc = 0; tc < 9; tc++) {
                                if (tr == r && tc == c) {
                                    continue;
                                }
                                String to = Coordinate.format(tr, tc);
                        Move m = new Move(from, to);
                        if (RuleValidator.isValidMove(board, m, color)
                                && RuleValidator.isMoveLegal(board, m, color)) {
                            return m;
                        }
                    }
                }
            }
        }
        return null;
    }
}
