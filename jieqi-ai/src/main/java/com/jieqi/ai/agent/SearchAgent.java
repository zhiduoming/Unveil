package com.jieqi.ai.agent;

import com.jieqi.ai.OptimizedAlphaBeta;
import com.jieqi.core.Move;

/**
 * 主搜索 Agent：Alpha-Beta + 置换表/启发式。
 */
public class SearchAgent implements JieqiSubAgent {

    private final OptimizedAlphaBeta search;

    public SearchAgent() {
        this(new OptimizedAlphaBeta());
    }

    public SearchAgent(OptimizedAlphaBeta search) {
        this.search = search;
    }

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
        long limit = ctx.getTimeLimitMs();
        OptimizedAlphaBeta.SearchResult result = search.search(ctx.getBoard(), ctx.getColor(), limit);
        return result.bestMove;
    }
}
