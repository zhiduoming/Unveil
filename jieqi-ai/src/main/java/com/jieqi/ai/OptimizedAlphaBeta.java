package com.jieqi.ai;

import com.jieqi.core.*;
import java.util.*;

public class OptimizedAlphaBeta {
    private static final int INF = Integer.MAX_VALUE / 2;
    private static final int MAX_DEPTH = 20;
    private static final int ROOT_TACTICAL_ORDER_DEPTH = MAX_DEPTH - 1;
    private static final int MAJOR_THREAT_PENALTY_LIMIT = 12000;
    // 长将规避：某根步执行后若使「该局面 + 待走方」重复计数将达此值且仍在将军，
    // 视为接近长将判负（阈值 6），对该步施加重罚使 AI 改走他步（绝杀步除外）。
    private static final int REPETITION_DANGER = 5;
    private static final int REPETITION_PENALTY = 100_000;
    private static final int ASPIRATION_WINDOW = 80;
    private TranspositionTable tt;
    private HistoryHeuristic history;
    private KillerHeuristic killers;
    private long startTime;
    private long timeLimit;
    private int nodesSearched;
    private int maxDepthReached;
    private boolean abortSearch;
    private Map<String, Integer> repetition;
    private int evalBias;

    public OptimizedAlphaBeta() {
        this.tt = new TranspositionTable();
        this.history = new HistoryHeuristic();
        this.killers = new KillerHeuristic(MAX_DEPTH);
    }

    public SearchResult search(Board board, int color, long timeLimitMs) {
        return search(board, color, timeLimitMs, null);
    }

    public SearchResult search(Board board, int color, long timeLimitMs, Map<String, Integer> repetition) {
        return search(board, color, timeLimitMs, repetition, 0);
    }

    public SearchResult search(Board board, int color, long timeLimitMs,
                               Map<String, Integer> repetition, int evalBias) {
        this.repetition = repetition;
        this.evalBias = evalBias;
        this.startTime = System.currentTimeMillis();
        this.timeLimit = Math.max(50L, timeLimitMs);
        this.nodesSearched = 0;
        this.maxDepthReached = 0;
        this.abortSearch = false;

        List<Move> moves = RuleValidator.generateLegalMoves(board, color);
        if (moves.isEmpty()) return new SearchResult(null, terminalNoLegalMovesScore(board, color));

        for (Move m : moves) {
            ChessPiece target = board.getPiece(m.getDestination());
            if (target != null && target.isRevealed() && target.getType() == ChessPiece.KING)
                return new SearchResult(m, INF - 1);
        }

        Move bestMove = moves.get(0);
        int bestScore = EnhancedEvaluator.evaluate(afterMove(board, bestMove), color);
        long hash = ZobristHash.computeHash(board);
        orderMoves(board, moves, color, ROOT_TACTICAL_ORDER_DEPTH, hash);
        Move ttBest = tt.getBestMove(hash);
        if (ttBest != null) orderMoveToFront(moves, ttBest);

        long lastDepthElapsed = 0L;
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            if (abortSearch) break;
            long depthStart = System.currentTimeMillis();
            int alpha;
            int beta;
            if (depth <= 1) {
                alpha = -INF;
                beta = INF;
            } else {
                alpha = Math.max(-INF, bestScore - ASPIRATION_WINDOW);
                beta = Math.min(INF, bestScore + ASPIRATION_WINDOW);
            }

            DepthResult depthResult = searchAtDepth(board, color, depth, moves, alpha, beta);
            if (!abortSearch && (depthResult.bestScore <= alpha || depthResult.bestScore >= beta)) {
                depthResult = searchAtDepth(board, color, depth, moves, -INF, INF);
            }

            if (!abortSearch) {
                maxDepthReached = depth;
                bestMove = depthResult.bestMove;
                bestScore = depthResult.bestScore;
                if (bestMove != null) tt.put(hash, depth, bestScore, TranspositionTable.EXACT, bestMove);
                if (Math.abs(bestScore) > INF - 1000) break;
                if (depth % 4 == 0) history.age();
                lastDepthElapsed = System.currentTimeMillis() - depthStart;
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = timeLimit - elapsed;
                if (depth >= 3 && remaining < lastDepthElapsed * 2) {
                    break;
                }
            }
        }
        System.out.println("[AI] 搜索完成: 深度=" + maxDepthReached + ", 节点=" + nodesSearched + ", 分数=" + bestScore);
        return new SearchResult(bestMove, bestScore);
    }

    private record DepthResult(int bestScore, Move bestMove) {}

    private DepthResult searchAtDepth(Board board, int color, int depth, List<Move> moves,
                                      int alpha, int beta) {
        int currentBest = -INF;
        Move currentBestMove = null;
        int alphaLocal = alpha;

        for (int i = 0; i < moves.size(); i++) {
            if (abortSearch) break;
            Move move = moves.get(i);
            ChessPiece captured = board.executeMove(move);
            nodesSearched++;
            int score;
            int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
            boolean repetitionRisk = isRepeatedCheckRisk(board, oppColor, repetition);
            if (RuleValidator.isCheckmate(board, oppColor)) score = INF - 1;
            else {
                if (i == 0) score = -alphaBeta(board, oppColor, depth - 1, -beta, -alphaLocal, true);
                else {
                    score = -alphaBeta(board, oppColor, depth - 1, -alphaLocal - 1, -alphaLocal, false);
                    if (score > alphaLocal && score < beta)
                        score = -alphaBeta(board, oppColor, depth - 1, -beta, -alphaLocal, true);
                }
            }
            board.undoMove(move, captured);
            if (repetitionRisk && Math.abs(score) < INF - 1000) {
                score -= REPETITION_PENALTY;
            }
            if (score > currentBest) {
                currentBest = score;
                currentBestMove = move;
            }
            if (score > alphaLocal) {
                history.recordMove(move, color, depth);
                alphaLocal = score;
            }
        }
        return new DepthResult(currentBest, currentBestMove);
    }

    /**
     * 判断「当前(已执行某根步后)局面」是否构成接近长将判负的重复将军：
     * 仍在将对方军，且该局面 + 待走方的重复计数 +1 将达到危险阈值。
     *
     * @param board      已执行候选步之后的棋盘
     * @param oppColor   候选步执行后轮到走子的一方（对手）
     * @param repetition 重复局面计数；为 null 时不规避
     */
    static boolean isRepeatedCheckRisk(Board board, int oppColor, Map<String, Integer> repetition) {
        if (repetition == null) return false;
        if (!RuleValidator.isInCheck(board, oppColor)) return false;
        String key = Board.positionKey(board, oppColor);
        return repetition.getOrDefault(key, 0) + 1 >= REPETITION_DANGER;
    }

    private int alphaBeta(Board board, int color, int depth, int alpha, int beta, boolean isPV) {
        if (abortSearch) return 0;
        if (System.currentTimeMillis() - startTime > timeLimit) { abortSearch = true; return 0; }

        long hash = ZobristHash.computeHash(board);
        TranspositionTable.Entry ttEntry = tt.get(hash);
        if (ttEntry != null && ttEntry.getDepth() >= depth && !isPV) {
            if (ttEntry.getFlag() == TranspositionTable.EXACT) return ttEntry.getScore();
            if (ttEntry.getFlag() == TranspositionTable.LOWER) alpha = Math.max(alpha, ttEntry.getScore());
            if (ttEntry.getFlag() == TranspositionTable.UPPER) beta = Math.min(beta, ttEntry.getScore());
            if (alpha >= beta) return ttEntry.getScore();
        }

        if (depth == 0) {
            nodesSearched++;
            return quiescenceSearch(board, color, alpha, beta, 3);
        }

        List<Move> moves = RuleValidator.generateLegalMoves(board, color);
        if (moves.isEmpty()) return terminalNoLegalMovesScore(board, color);

        orderMoves(board, moves, color, depth, hash);
        int bestScore = -INF;
        Move bestMove = null;
        int alphaOrig = alpha;
        int searched = 0;

        for (Move move : moves) {
            // 超时后立即停止：避免对剩余走法继续跑昂贵的 isCheckmate/generateAllMoves，
            // 否则单步思考可能远超预算（这是 AI 偶发超时判负的根因之一）。
            if (abortSearch) break;
            ChessPiece captured = board.executeMove(move);
            nodesSearched++;
            int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
            int score;
            if (captured != null && captured.isRevealed() && captured.getType() == ChessPiece.KING) {
                score = INF - 100;
                board.undoMove(move, captured);
                bestScore = score; bestMove = move;
                break;
            }
            if (RuleValidator.isCheckmate(board, oppColor)) {
                score = INF - 100;
                board.undoMove(move, captured);
                bestScore = score; bestMove = move;
                break;
            }
            boolean captureOrCheck = captured != null || RuleValidator.isInCheck(board, oppColor);
            int reduction = 0;
            if (!captureOrCheck && searched >= 4 && depth >= 3 && !isPV) {
                reduction = 1;
            }
            if (searched == 0) {
                score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, isPV);
            } else {
                score = -alphaBeta(board, oppColor, depth - 1 - reduction, -alpha - 1, -alpha, false);
                if (score > alpha && score < beta) {
                    score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, true);
                }
            }
            board.undoMove(move, captured);
            searched++;
            if (score > bestScore) { bestScore = score; bestMove = move; }
            if (score > alpha) alpha = score;
            if (alpha >= beta) {
                ChessPiece target = board.getPiece(move.getDestination());
                if (target == null && !move.isFlipOnly()) killers.addKiller(move, depth);
                history.recordMove(move, color, depth);
                break;
            }
        }

        int flag;
        if (bestScore <= alphaOrig) flag = TranspositionTable.UPPER;
        else if (bestScore >= beta) flag = TranspositionTable.LOWER;
        else flag = TranspositionTable.EXACT;
        tt.put(hash, depth, bestScore, flag, bestMove);
        return bestScore;
    }

    private int quiescenceSearch(Board board, int color, int alpha, int beta, int depth) {
        if (abortSearch) return 0;
        if (depth <= 0) return EnhancedEvaluator.evaluate(board, color) + evalBias;
        int standPat = EnhancedEvaluator.evaluate(board, color) + evalBias;
        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        List<Move> captureMoves = new ArrayList<>();
        for (Move m : RuleValidator.generateLegalMoves(board, color)) {
            if (board.getPiece(m.getDestination()) != null) captureMoves.add(m);
        }
        if (captureMoves.isEmpty()) return standPat;
        // SEE 排序 + 过滤：把明显赚的吃子排前面，跳过明显亏的吃子（SEE < 0）
        // 这样 quiescence 不会被 "拿車换兵" 之类的烂交换污染。
        int[] seeScores = new int[captureMoves.size()];
        for (int i = 0; i < captureMoves.size(); i++) {
            seeScores[i] = StaticExchangeEvaluator.see(board, captureMoves.get(i));
        }
        // 简单按 SEE 降序排
        for (int i = 1; i < captureMoves.size(); i++) {
            for (int j = i; j > 0 && seeScores[j] > seeScores[j - 1]; j--) {
                int ts = seeScores[j]; seeScores[j] = seeScores[j - 1]; seeScores[j - 1] = ts;
                Move tm = captureMoves.get(j);
                captureMoves.set(j, captureMoves.get(j - 1));
                captureMoves.set(j - 1, tm);
            }
        }
        for (int i = 0; i < captureMoves.size(); i++) {
            if (abortSearch) break;
            if (seeScores[i] < 0) break;                    // SEE < 0 → 直接跳过
            Move move = captureMoves.get(i);
            ChessPiece captured = board.executeMove(move);
            int score = -quiescenceSearch(board, color==ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED,
                                          -beta, -alpha, depth-1);
            board.undoMove(move, captured);
            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }
        return alpha;
    }

    private void orderMoves(Board board, List<Move> moves, int color, int depth, long hash) {
        Move ttBest = tt.getBestMove(hash);
        Map<Move, Integer> scores = new HashMap<>();
        for (Move move : moves) {
            int sc = 0;
            if (ttBest != null && move.getSource().equals(ttBest.getSource()) && move.getDestination().equals(ttBest.getDestination()))
                sc += 100000;
            int[] src = ChessPiece.fromCoord(move.getSource()), dst = ChessPiece.fromCoord(move.getDestination());
            ChessPiece target = board.getPiece(dst[0], dst[1]);
            ChessPiece piece = board.getPiece(src[0], src[1]);
            // 吃子用便宜的 MVV-LVA（Most Valuable Victim, Least Valuable Attacker）
            // 排序：吃高价值子 + 用低价值子优先。SEE 在 quiescence 里跑（那里更值）。
            // 之前在这里跑 SEE 拖垮性能：每个节点 N 次 board copy + generateAllMoves。
            if (target != null && piece != null) {
                sc += target.getValue() * 10 - piece.getValue();
                if (target.isRevealed() && target.getType() == ChessPiece.KING) sc += 100000;
            }
            if (move.isFlipOnly()) sc += 5000;
            // 暗子原本加 +3000 过高，会让 AI 优先翻暗子而错过更好的吃子；降到 800
            else if (piece != null && !piece.isRevealed()) sc += 800;
            sc += killers.getKillerScore(move, depth);
            sc += history.getScore(move, color);
            sc += (8 - (Math.abs(dst[0]-4) + Math.abs(dst[1]-4))) * 3;
            if (depth >= ROOT_TACTICAL_ORDER_DEPTH) {
                sc -= majorPieceThreatPenaltyAfter(board, move, color);
            }
            scores.put(move, sc);
        }
        moves.sort((a,b) -> scores.getOrDefault(b,0) - scores.getOrDefault(a,0));
    }

    private int majorPieceThreatPenaltyAfter(Board board, Move move, int color) {
        ChessPiece captured = board.executeMove(move);
        int penalty = majorPieceThreatPenalty(board, color);
        board.undoMove(move, captured);
        return penalty;
    }

    private int majorPieceThreatPenalty(Board board, int color) {
        int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
        int worstPenalty = 0;
        for (Move oppMove : RuleValidator.generateLegalMoves(board, oppColor)) {
            int[] src = ChessPiece.fromCoord(oppMove.getSource());
            int[] dst = ChessPiece.fromCoord(oppMove.getDestination());
            ChessPiece attacker = board.getPiece(src[0], src[1]);
            ChessPiece target = board.getPiece(dst[0], dst[1]);
            if (attacker == null || target == null || target.getColor() != color) {
                continue;
            }
            if (!target.isRevealed() || !isMajorPiece(target.getType())) {
                continue;
            }
            int targetValue = target.getValue();
            int attackerValue = Math.max(1, attacker.getValue());
            int exchangeGap = targetValue - attackerValue;
            int penalty = exchangeGap > 0 ? exchangeGap * 8 : targetValue / 3;
            worstPenalty = Math.max(worstPenalty, penalty);
        }
        return Math.min(worstPenalty, MAJOR_THREAT_PENALTY_LIMIT);
    }

    private boolean isMajorPiece(int type) {
        return type == ChessPiece.ROOK || type == ChessPiece.CANNON || type == ChessPiece.KNIGHT;
    }

    private int terminalNoLegalMovesScore(Board board, int color) {
        return RuleValidator.isInCheck(board, color) ? -INF + 1000 : -INF + 2000;
    }

    private Board afterMove(Board board, Move move) {
        Board next = new Board(board);
        next.executeMove(move);
        return next;
    }

    private void orderMoveToFront(List<Move> moves, Move best) {
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            if (m.getSource().equals(best.getSource()) && m.getDestination().equals(best.getDestination())) {
                if (i > 0) { moves.remove(i); moves.add(0, m); }
                break;
            }
        }
    }

    public int getNodesSearched() { return nodesSearched; }
    public int getMaxDepthReached() { return maxDepthReached; }
    public void clearHeuristics() { tt.clear(); history.clear(); killers.clear(); }

    public static class SearchResult {
        public final Move bestMove;
        public final int score;
        public SearchResult(Move bestMove, int score) { this.bestMove = bestMove; this.score = score; }
    }
}
