package com.jieqi.ai.agent;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;

/**
 * 根据暗子池期望值修正搜索前的局面评估倾向（不直接选着）。
 */
public class ProbabilityAgent implements JieqiSubAgent {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(AgentContext ctx) {
        return ctx.getBoard().getUnrevealedPieces(ctx.getColor()).size() > 0;
    }

    @Override
    public Move contribute(AgentContext ctx) {
        Board board = ctx.getBoard();
        int color = ctx.getColor();
        int ev = board.getExpectedValue(color);
        int oppEv = board.getExpectedValue(
                color == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED);
        ctx.setProbabilityBias(ev - oppEv);
        return null;
    }
}
