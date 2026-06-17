package com.jieqi.ai.bot;

/** 各难度 AI 的运行参数。 */
public final class AiConfig {

    private final AiLevel level;
    private final long timeLimitMs;
    private final int topKRandom;
    private final int beliefSamples;
    private final int maxCandidatesForBelief;

    private AiConfig(AiLevel level, long timeLimitMs, int topKRandom,
                     int beliefSamples, int maxCandidatesForBelief) {
        this.level = level;
        this.timeLimitMs = timeLimitMs;
        this.topKRandom = topKRandom;
        this.beliefSamples = beliefSamples;
        this.maxCandidatesForBelief = maxCandidatesForBelief;
    }

    public static AiConfig forLevel(AiLevel level, long humanBudgetMs) {
        return switch (level) {
            case EASY -> new AiConfig(level, Math.min(800L, humanBudgetMs), 5, 0, 0);
            case HARD -> new AiConfig(level, humanBudgetMs, 1, 8, 15);
            default -> new AiConfig(level, humanBudgetMs, 1, 0, 0);
        };
    }

    public AiLevel level() { return level; }
    public long timeLimitMs() { return timeLimitMs; }
    public int topKRandom() { return topKRandom; }
    public int beliefSamples() { return beliefSamples; }
    public int maxCandidatesForBelief() { return maxCandidatesForBelief; }
}
