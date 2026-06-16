package com.jieqi.ai.bot;

import com.jieqi.core.Board;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;

import java.util.Map;

/** 按难度构造 {@link AiBot}。 */
public final class AiBotFactory {

    private AiBotFactory() {}

    public static AiBot create(AiLevel level, long humanBudgetMs) {
        AiConfig config = AiConfig.forLevel(level, humanBudgetMs);
        return switch (level) {
            case EASY -> new EasyRuleBot(config);
            case HARD -> new BeliefAlphaBetaBot(config);
            default -> new AlphaBetaBot(config);
        };
    }

    public static Move selectWithFallback(AiBot bot, Board board, int color, long budget,
                                          Map<String, Integer> repetition) {
        Move move = null;
        try {
            move = bot.selectMove(board, color, budget, repetition);
        } catch (Exception ignored) {
            // 降级
        }
        if (move != null && RuleValidator.isMoveLegal(board, move, color)) {
            return move;
        }
        var legal = RuleValidator.generateLegalMoves(board.createAiPublicView(color), color);
        return legal.isEmpty() ? null : legal.get(0);
    }
}
