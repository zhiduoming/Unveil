package com.jieqi.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EndgameJudgeTest {

    @Test
    void checkmateEndsWithRedWin() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 0), 0, 0);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 0, 1), 0, 1);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 1, 0), 1, 0);

        Map<String, Integer> rep = new HashMap<>();
        EndgameJudge.Verdict v = EndgameJudge.checkAfterMove(
                board, ChessPiece.RED, null, rep, hash(board, ChessPiece.BLACK),
                new Move("a9", "a8"));

        assertNotNull(v);
        assertEquals(Game.GameStatus.RED_WIN, v.status());
        assertEquals(EndgameJudge.ProtocolReason.CHECKMATE, v.reasonCode());
    }

    @Test
    void stalemateEndsWithRedWin() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 0), 0, 0);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 8, 2), 8, 2);

        assertTrue(RuleValidator.isStalemate(board, ChessPiece.BLACK));

        Map<String, Integer> rep = new HashMap<>();
        EndgameJudge.Verdict v = EndgameJudge.checkAfterMove(
                board, ChessPiece.RED, null, rep, hash(board, ChessPiece.BLACK),
                new Move("c2", "c1"));

        assertNotNull(v);
        assertEquals(Game.GameStatus.RED_WIN, v.status());
        assertEquals(EndgameJudge.ProtocolReason.STALEMATE, v.reasonCode());
    }

    @Test
    void kingCapturedEndsGame() {
        Board board = new Board();
        ChessPiece king = new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 9, 4);
        Map<String, Integer> rep = new HashMap<>();

        EndgameJudge.Verdict v = EndgameJudge.checkAfterMove(
                board, ChessPiece.RED, king, rep, hash(board, ChessPiece.BLACK), null);

        assertNotNull(v);
        assertEquals(Game.GameStatus.RED_WIN, v.status());
        assertEquals(EndgameJudge.ProtocolReason.KING_CAPTURED, v.reasonCode());
        assertTrue(rep.isEmpty());
    }

    @Test
    void longCheckLossOnSixthRepetition() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 8, 4), 8, 4);

        assertTrue(RuleValidator.isInCheck(board, ChessPiece.BLACK));
        assertFalse(RuleValidator.isCheckmate(board, ChessPiece.BLACK));

        Map<String, Integer> rep = new HashMap<>();
        String h = hash(board, ChessPiece.BLACK);
        Move checkMove = new Move("e2", "e1");
        EndgameJudge.Verdict last = null;
        for (int i = 0; i < 6; i++) {
            last = EndgameJudge.checkAfterMove(board, ChessPiece.RED, null, rep, h, checkMove);
        }
        assertNotNull(last);
        assertEquals(Game.GameStatus.BLACK_WIN, last.status());
        assertEquals(EndgameJudge.ProtocolReason.REPETITION_LOSS, last.reasonCode());
    }

    @Test
    void pawnLongChaseDrawOnSixthRepetition() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.ADVISOR, ChessPiece.RED, true, 6, 4), 6, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.BLACK, true, 4, 6), 4, 6);
        board.placePiece(new ChessPiece(ChessPiece.PAWN, ChessPiece.RED, true, 4, 4), 4, 4);

        Move chase = new Move("e5", "f5");
        board.executeMove(chase);

        Map<String, Integer> rep = new HashMap<>();
        String h = hash(board, ChessPiece.BLACK);
        assertNotNull(EndgameJudge.findChaseTarget(board, chase, ChessPiece.RED));
        assertTrue(EndgameJudge.isPawnMover(board, chase));

        EndgameJudge.Verdict last = null;
        for (int i = 0; i < 6; i++) {
            last = EndgameJudge.checkAfterMove(board, ChessPiece.RED, null, rep, h, chase);
        }
        assertNotNull(last);
        assertEquals(Game.GameStatus.DRAW, last.status());
        assertEquals(EndgameJudge.ProtocolReason.REPETITION_DRAW, last.reasonCode());
    }

    @Test
    void bareRepetitionWithoutCheckOrChaseDoesNotEnd() {
        Board board = new Board();
        Map<String, Integer> rep = new HashMap<>();
        String h = "same|0";
        EndgameJudge.Verdict last = null;
        for (int i = 0; i < 6; i++) {
            last = EndgameJudge.checkAfterMove(board, ChessPiece.RED, null, rep, h, null);
        }
        assertNull(last);
        assertEquals(6, rep.get(h));
    }

    private static Board emptyBoard() {
        Board board = new Board();
        board.clearAllPieces();
        return board;
    }

    private static String hash(Board board, int sideToMove) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null) sb.append(".");
                else sb.append(p.getColor()).append(p.isRevealed() ? p.getType() : "?");
            }
        }
        sb.append('|').append(sideToMove);
        return sb.toString();
    }
}
