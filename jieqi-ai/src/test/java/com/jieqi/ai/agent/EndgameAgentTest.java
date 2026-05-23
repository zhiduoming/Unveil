package com.jieqi.ai.agent;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndgameAgentTest {

    @Test
    void inactiveWhenManyPiecesOnBoard() {
        Board board = new Board();
        AgentContext ctx = new AgentContext(board, ChessPiece.RED, 5000);
        EndgameAgent agent = new EndgameAgent();
        assertFalse(agent.supports(ctx));
    }
}
