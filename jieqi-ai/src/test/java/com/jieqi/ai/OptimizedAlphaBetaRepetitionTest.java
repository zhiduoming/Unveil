package com.jieqi.ai;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 长将规避判定：红车将军黑将的局面下，按重复计数是否接近判负阈值（6）判断风险。
 */
class OptimizedAlphaBetaRepetitionTest {

    /** 红车 (1,4) 紧贴黑将 (0,4) 上方同列将军。 */
    private static Board checkingBoard() {
        Board b = new Board();
        b.clearAllPieces();
        b.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        b.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 1, 4), 1, 4);
        b.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 0), 9, 0);
        return b;
    }

    @Test
    void nullRepetitionNeverRisk() {
        assertFalse(OptimizedAlphaBeta.isRepeatedCheckRisk(checkingBoard(), ChessPiece.BLACK, null));
    }

    @Test
    void belowThresholdNotRisk() {
        Board b = checkingBoard();
        Map<String, Integer> rep = new HashMap<>();
        rep.put(Board.positionKey(b, ChessPiece.BLACK), 3); // 3+1=4 < 5
        assertFalse(OptimizedAlphaBeta.isRepeatedCheckRisk(b, ChessPiece.BLACK, rep));
    }

    @Test
    void reachingDangerIsRisk() {
        Board b = checkingBoard();
        Map<String, Integer> rep = new HashMap<>();
        rep.put(Board.positionKey(b, ChessPiece.BLACK), 4); // 4+1=5 >= 5
        assertTrue(OptimizedAlphaBeta.isRepeatedCheckRisk(b, ChessPiece.BLACK, rep));
    }

    @Test
    void notInCheckNeverRisk() {
        Board b = new Board();
        b.clearAllPieces();
        b.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        b.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 0), 9, 0);
        Map<String, Integer> rep = new HashMap<>();
        rep.put(Board.positionKey(b, ChessPiece.BLACK), 9); // 计数高但没将军
        assertFalse(OptimizedAlphaBeta.isRepeatedCheckRisk(b, ChessPiece.BLACK, rep));
    }
}
