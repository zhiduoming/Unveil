package com.jieqi.ai.agent;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentOrchestratorTest {

    @Test
    void usesFirstAgentThatReturnsMove() {
        Move stubMove = findAnyLegalMove(new Board(), ChessPiece.RED);
        assertNotNull(stubMove);

        JieqiSubAgent stub = new JieqiSubAgent() {
            @Override
            public int priority() {
                return 100;
            }

            @Override
            public boolean supports(AgentContext ctx) {
                return true;
            }

            @Override
            public Move contribute(AgentContext ctx) {
                return stubMove;
            }
        };

        AgentOrchestrator orchestrator = new AgentOrchestrator(List.of(stub));
        Move chosen = orchestrator.selectMove(new Board(), ChessPiece.RED, 1000);
        assertEquals(stubMove.getSource(), chosen.getSource());
        assertEquals(stubMove.getDestination(), chosen.getDestination());
    }

    @Test
    void fallsBackToAnyLegalMoveWhenAgentsReturnNull() {
        JieqiSubAgent nullAgent = new JieqiSubAgent() {
            @Override
            public int priority() {
                return 100;
            }

            @Override
            public boolean supports(AgentContext ctx) {
                return true;
            }

            @Override
            public Move contribute(AgentContext ctx) {
                return null;
            }
        };

        Board board = new Board();
        AgentOrchestrator orchestrator = new AgentOrchestrator(List.of(nullAgent));
        Move chosen = orchestrator.selectMove(board, ChessPiece.RED, 1);
        assertNotNull(chosen);
        assertTrue(RuleValidator.isValidMove(board, chosen, ChessPiece.RED));
        assertTrue(RuleValidator.isMoveLegal(board, chosen, ChessPiece.RED));
    }

    private static Move findAnyLegalMove(Board board, int color) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece piece = board.getPiece(r, c);
                if (piece == null || piece.getColor() != color) {
                    continue;
                }
                String src = ChessPiece.toCoord(r, c);
                for (int dr = 0; dr < 10; dr++) {
                    for (int dc = 0; dc < 9; dc++) {
                        String dst = ChessPiece.toCoord(dr, dc);
                        Move move = new Move(src, dst);
                        if (RuleValidator.isValidMove(board, move, color)
                                && RuleValidator.isMoveLegal(board, move, color)) {
                            return move;
                        }
                    }
                }
            }
        }
        return null;
    }
}
