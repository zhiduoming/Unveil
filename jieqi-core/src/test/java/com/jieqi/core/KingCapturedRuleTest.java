package com.jieqi.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 将帅安全规则：不能主动送将；直接吃到将/帅仍以 KING_CAPTURED 结束。 */
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
    void gameProcessMoveRejectsExposingKing() {
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
        assertEquals("不能送将", err);
        assertEquals(Game.GameStatus.PLAYING, game.getStatus());
    }

    private static Board emptyBoard() {
        Board board = new Board();
        board.clearAllPieces();
        return board;
    }
}
