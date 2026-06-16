package com.jieqi.ai.bot;

import com.jieqi.ai.JieqiAgent;
import com.jieqi.core.Board;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;

import java.util.Map;

/** 标准：Alpha-Beta + 多 Agent 编排（脱敏公开展示棋盘）。 */
public final class AlphaBetaBot implements AiBot {

    private final AiConfig config;
    private final JieqiAgent agent;

    public AlphaBetaBot(AiConfig config) {
        this(config, new JieqiAgent());
    }

    public AlphaBetaBot(AiConfig config, JieqiAgent agent) {
        this.config = config;
        this.agent = agent;
    }

    @Override
    public AiLevel level() {
        return AiLevel.MEDIUM;
    }

    @Override
    public Move selectMove(Board authoritativeBoard, int color, long timeLimitMs,
                           Map<String, Integer> repetition) {
        long budget = Math.min(timeLimitMs, config.timeLimitMs());
        Move move = agent.selectMove(authoritativeBoard, color, budget, repetition);
        if (move != null && RuleValidator.isMoveLegal(authoritativeBoard, move, color)) {
            return move;
        }
        return fallback(authoritativeBoard, color);
    }

    private Move fallback(Board board, int color) {
        var legal = RuleValidator.generateLegalMoves(board.createAiPublicView(color), color);
        return legal.isEmpty() ? null : legal.get(0);
    }
}
