package com.jieqi.ai.agent;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProbabilityAgentTest {

    @Test
    void setsProbabilityBiasWhenDarkPiecesRemain() {
        Board board = new Board();
        AgentContext ctx = new AgentContext(board, ChessPiece.RED, 1000);
        ProbabilityAgent agent = new ProbabilityAgent();

        assertTrue(agent.supports(ctx));
        assertNull(agent.contribute(ctx));
        int expectedBias = board.getExpectedValue(ChessPiece.RED)
                - board.getExpectedValue(ChessPiece.BLACK);
        assertEquals(expectedBias, ctx.getProbabilityBias());
    }
}
