package com.jieqi.ai;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JieqiAgentTest {

    @Test
    void selectsLegalMoveWithinTimeLimit() {
        Board board = new Board();
        JieqiAgent agent = new JieqiAgent();
        Move move = agent.selectMove(board, ChessPiece.RED, 3_000L);
        assertNotNull(move);
        assertNotNull(move.getSource());
        assertNotNull(move.getDestination());
    }
}
