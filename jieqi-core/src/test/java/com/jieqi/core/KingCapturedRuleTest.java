package com.jieqi.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Q2：允许送将，对方下一步吃明将/帅以 KING_CAPTURED 获胜。
 */
class KingCapturedRuleTest {

    @Test
    void allowsExposeKingThenCaptureWins() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 3), 0, 3);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);

        Move expose = new Move("e5", "f5");
        assertTrue(RuleValidator.isValidMove(board, expose, ChessPiece.RED));
        board.executeMove(expose);
        assertTrue(RuleValidator.isInCheck(board, ChessPiece.RED));

        Move captureKing = new Move("e9", "e0");
        assertTrue(RuleValidator.isValidMove(board, captureKing, ChessPiece.BLACK));
        ChessPiece captured = board.executeMove(captureKing);
        assertNotNull(captured);
        assertEquals(ChessPiece.KING, captured.getType());

        Map<String, Integer> rep = new HashMap<>();
        EndgameJudge.Verdict verdict = EndgameJudge.checkAfterMove(
                board, ChessPiece.BLACK, captured, rep, "h|1", captureKing);
        assertNotNull(verdict);
        assertEquals(Game.GameStatus.BLACK_WIN, verdict.status());
        assertEquals(EndgameJudge.ProtocolReason.KING_CAPTURED, verdict.reasonCode());
    }

    @Test
    void gameProcessMoveAllowsExposingKing() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);

        Game game = new Game("q2-test");
        game.connectPlayer(ChessPiece.RED);
        game.connectPlayer(ChessPiece.BLACK);
        game.replaceBoard(board);
        game.setCurrentTurn(ChessPiece.RED);

        String err = game.processMove(new Move("e5", "f5"), ChessPiece.RED);
        assertNull(err, "送将着法不得被「走子后将被将军」拒绝");
        assertEquals(Game.GameStatus.PLAYING, game.getStatus());
    }

    private static Board emptyBoard() {
        Board board = new Board();
        board.clearAllPieces();
        return board;
    }
}
