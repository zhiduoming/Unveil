package com.jieqi.ai;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnhancedEvaluatorTest {

    @Test
    void evaluationIsAntisymmetricBetweenColors() {
        Board board = new Board();
        int redView = EnhancedEvaluator.evaluate(board, ChessPiece.RED);
        int blackView = EnhancedEvaluator.evaluate(board, ChessPiece.BLACK);
        assertEquals(-redView, blackView);
    }
}
