package com.jieqi.ai.bot;

import com.jieqi.ai.OptimizedAlphaBeta;
import com.jieqi.ai.belief.BoardSampler;
import com.jieqi.core.Board;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 挑战：对候选走法做多采样信念评估（对手暗子随机分配），再选期望分最高步。
 */
public final class BeliefAlphaBetaBot implements AiBot {

    private final AiConfig config;
    private final Random rng = new Random(0xBE11EF01L);

    public BeliefAlphaBetaBot(AiConfig config) {
        this.config = config;
    }

    @Override
    public AiLevel level() {
        return AiLevel.HARD;
    }

    @Override
    public Move selectMove(Board authoritativeBoard, int color, long timeLimitMs,
                           Map<String, Integer> repetition) {
        Board publicView = authoritativeBoard.createAiPublicView(color);
        List<Move> candidates = RuleValidator.generateLegalMoves(publicView, color);
        if (candidates.isEmpty()) {
            return null;
        }
        MoveOrderer.sortByHeuristic(publicView, candidates, color);

        long budget = Math.min(timeLimitMs, config.timeLimitMs());
        int samples = Math.max(1, config.beliefSamples());
        int candidateLimit = config.maxCandidatesForBelief();
        if (budget < 3000) {
            samples = Math.min(samples, 2);
            candidateLimit = Math.min(candidateLimit, 4);
        }

        int limit = Math.min(candidateLimit, candidates.size());
        candidates = candidates.subList(0, limit);

        long deadline = System.currentTimeMillis() + budget;
        Map<String, Double> scores = new HashMap<>();

        for (int candidateIndex = 0; candidateIndex < candidates.size(); candidateIndex++) {
            if (System.currentTimeMillis() >= deadline) {
                break;
            }
            Move candidate = candidates.get(candidateIndex);
            double sum = 0;
            int used = 0;
            for (int s = 0; s < samples; s++) {
                if (System.currentTimeMillis() >= deadline) {
                    break;
                }
                Board sample = BoardSampler.fromPublicView(publicView, color, rng);
                Board.MoveSnapshot snap = sample.makeMove(candidate);

                long remaining = deadline - System.currentTimeMillis();
                int remainingSlots = samples - s + (candidates.size() - candidateIndex - 1) * samples;
                long perSample = Math.max(30L, remaining / Math.max(1, remainingSlots));

                OptimizedAlphaBeta localSearch = new OptimizedAlphaBeta();
                OptimizedAlphaBeta.SearchResult result =
                        localSearch.search(sample, opp(color), perSample, repetition);
                sample.unmakeMove(snap);
                sum -= result.score;
                used++;
            }
            if (used > 0) {
                scores.put(key(candidate), sum / used);
            }
        }

        Move best = candidates.get(0);
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Move m : candidates) {
            double sc = scores.getOrDefault(key(m), Double.NEGATIVE_INFINITY);
            if (sc > bestScore) {
                bestScore = sc;
                best = m;
            }
        }
        if (RuleValidator.isMoveLegal(authoritativeBoard, best, color)) {
            return best;
        }
        return new AlphaBetaBot(config).selectMove(authoritativeBoard, color, timeLimitMs, repetition);
    }

    private static int opp(int color) {
        return color == com.jieqi.core.ChessPiece.RED
                ? com.jieqi.core.ChessPiece.BLACK : com.jieqi.core.ChessPiece.RED;
    }

    private static String key(Move m) {
        return m.getSource() + ">" + m.getDestination();
    }
}
