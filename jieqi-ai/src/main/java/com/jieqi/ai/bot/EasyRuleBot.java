package com.jieqi.ai.bot;

import com.jieqi.core.Board;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** 入门：启发式评分 + TopK 随机，保证合法且不吃透视。 */
public final class EasyRuleBot implements AiBot {

    private final AiConfig config;

    public EasyRuleBot(AiConfig config) {
        this.config = config;
    }

    @Override
    public AiLevel level() {
        return AiLevel.EASY;
    }

    @Override
    public Move selectMove(Board authoritativeBoard, int color, long timeLimitMs,
                           Map<String, Integer> repetition) {
        Board aiBoard = authoritativeBoard.createAiPublicView(color);
        List<Move> moves = RuleValidator.generateLegalMoves(aiBoard, color);
        if (moves.isEmpty()) {
            return null;
        }
        MoveOrderer.sortByHeuristic(aiBoard, moves, color);
        if (ThreadLocalRandom.current().nextDouble() < 0.30) {
            return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
        }
        int k = Math.min(config.topKRandom(), moves.size());
        List<Move> top = new ArrayList<>(moves.subList(0, k));
        return top.get(ThreadLocalRandom.current().nextInt(top.size()));
    }
}
