package com.jieqi.ai.agent;

import com.jieqi.ai.OptimizedAlphaBeta;
import com.jieqi.core.Move;

/**
 * 残局（子力较少）时加深搜索时间权重。
 */
public class EndgameAgent implements JieqiSubAgent {

    private static final int ENDGAME_PIECE_THRESHOLD = 12;
    private final OptimizedAlphaBeta search = new OptimizedAlphaBeta();

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public boolean supports(AgentContext ctx) {
        return ctx.pieceCount() <= ENDGAME_PIECE_THRESHOLD;
    }

    @Override
    public Move contribute(AgentContext ctx) {
        long limit = Math.min(ctx.getTimeLimitMs(), 30_000L);
        OptimizedAlphaBeta.SearchResult result = search.search(ctx.getBoard(), ctx.getColor(), limit);
        return result.bestMove;
    }
}
