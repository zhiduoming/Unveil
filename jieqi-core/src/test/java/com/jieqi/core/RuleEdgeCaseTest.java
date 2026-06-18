package com.jieqi.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 规则边界用例矩阵（chanpintasks P0-3）。 */
class RuleEdgeCaseTest {

    @Test
    void kingsFacingSameFileMoveIsIllegal() {
        Board board = twoKings();
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);
        Move openFile = new Move("e5", "f5");
        assertTrue(RuleValidator.isValidMove(board, openFile, ChessPiece.RED));
        assertFalse(RuleValidator.isMoveLegal(board, openFile, ChessPiece.RED));
    }

    @Test
    void suicideMoveIsIllegal() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.BLACK, true, 8, 3), 8, 3);
        Move suicide = new Move("e0", "e1");
        assertTrue(RuleValidator.isValidMove(board, suicide, ChessPiece.RED));
        assertFalse(RuleValidator.isMoveLegal(board, suicide, ChessPiece.RED));
    }

    @Test
    void whenInCheckOnlyEscapeMovesAreLegal() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 8, 4), 8, 4);
        assertTrue(RuleValidator.isInCheck(board, ChessPiece.BLACK));

        List<Move> strict = RuleValidator.generateStrictLegalMoves(board, ChessPiece.BLACK);
        assertFalse(strict.isEmpty());
        java.util.Set<String> strictKeys = new java.util.HashSet<>();
        for (Move m : strict) {
            strictKeys.add(m.getSource() + "->" + m.getDestination());
            assertTrue(RuleValidator.isMoveLegal(board, m, ChessPiece.BLACK));
            ChessPiece cap = board.executeMove(m);
            assertFalse(RuleValidator.isInCheck(board, ChessPiece.BLACK));
            board.undoMove(m, cap);
        }
        for (Move m : RuleValidator.generateAllMoves(board, ChessPiece.BLACK)) {
            if (strictKeys.contains(m.getSource() + "->" + m.getDestination())) {
                continue;
            }
            assertFalse(RuleValidator.isMoveLegal(board, m, ChessPiece.BLACK));
        }
    }

    @Test
    void checkmateReturnsCheckmateVerdict() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 0), 0, 0);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 0, 1), 0, 1);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 1, 0), 1, 0);

        EndgameJudge.Verdict v = EndgameJudge.checkAfterMove(
                board, ChessPiece.RED, null, new HashMap<>(), hash(board, ChessPiece.BLACK),
                new Move("a9", "a8"));
        assertNotNull(v);
        assertEquals(Game.GameStatus.RED_WIN, v.status());
        assertEquals(EndgameJudge.ProtocolReason.CHECKMATE, v.reasonCode());
    }

    @Test
    void stalemateReturnsStalemateVerdict() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 0), 0, 0);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 8, 2), 8, 2);
        assertTrue(RuleValidator.isStalemate(board, ChessPiece.BLACK));

        EndgameJudge.Verdict v = EndgameJudge.checkAfterMove(
                board, ChessPiece.RED, null, new HashMap<>(), hash(board, ChessPiece.BLACK),
                new Move("c2", "c1"));
        assertNotNull(v);
        assertEquals(Game.GameStatus.RED_WIN, v.status());
        assertEquals(EndgameJudge.ProtocolReason.STALEMATE, v.reasonCode());
    }

    @Test
    void capturingKingIsValidMove() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 1, 4), 1, 4);
        Move captureKing = new Move("e8", "e9");
        assertTrue(RuleValidator.isValidMove(board, captureKing, ChessPiece.RED));
    }

    @Test
    void noCaptureDrawAtEightyHalfMoves() {
        Board board = new Board();
        board.setNoCaptureCount(80);
        EndgameJudge.Verdict v = EndgameJudge.checkAfterMove(
                board, ChessPiece.RED, null, new HashMap<>(), hash(board, ChessPiece.BLACK), null);
        assertNotNull(v);
        assertEquals(Game.GameStatus.DRAW, v.status());
        assertEquals(EndgameJudge.ProtocolReason.NO_CAPTURE_DRAW, v.reasonCode());
    }

    @Test
    void longCheckLossOnSixthRepetition() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 8, 4), 8, 4);

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
    void longChaseLossOnSixthRepetition() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.ADVISOR, ChessPiece.RED, true, 6, 4), 6, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.BLACK, true, 4, 6), 4, 6);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);

        Move chase = new Move("e5", "f5");
        board.executeMove(chase);
        assertNotNull(EndgameJudge.findChaseTarget(board, chase, ChessPiece.RED));
        assertFalse(EndgameJudge.isPawnMover(board, chase));

        Map<String, Integer> rep = new HashMap<>();
        String h = hash(board, ChessPiece.BLACK);
        EndgameJudge.Verdict last = null;
        for (int i = 0; i < 6; i++) {
            last = EndgameJudge.checkAfterMove(board, ChessPiece.RED, null, rep, h, chase);
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
        EndgameJudge.Verdict last = null;
        for (int i = 0; i < 6; i++) {
            last = EndgameJudge.checkAfterMove(board, ChessPiece.RED, null, rep, h, chase);
        }
        assertNotNull(last);
        assertEquals(Game.GameStatus.DRAW, last.status());
        assertEquals(EndgameJudge.ProtocolReason.REPETITION_DRAW, last.reasonCode());
    }

    @Test
    void aiSearchMakeUnmakePreservesBoardHash() {
        Board board = new Board();
        String key = Board.positionKey(board, ChessPiece.RED);
        for (Move move : RuleValidator.generateStrictLegalMoves(board, ChessPiece.RED)) {
            Board.MoveSnapshot snap = board.makeMove(move);
            board.unmakeMove(snap);
        }
        assertEquals(key, Board.positionKey(board, ChessPiece.RED));
        assertEquals(0, board.getMoveCount());
    }

    private static Board emptyBoard() {
        Board board = new Board();
        board.clearAllPieces();
        return board;
    }

    private static Board twoKings() {
        Board board = emptyBoard();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        return board;
    }

    private static String hash(Board board, int sideToMove) {
        return Board.positionKey(board, sideToMove);
    }
}
