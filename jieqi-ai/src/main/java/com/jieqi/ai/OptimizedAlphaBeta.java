package com.jieqi.ai;

import com.jieqi.core.*;
import java.util.*;

public class OptimizedAlphaBeta {
    private static final int INF = Integer.MAX_VALUE / 2;
    private static final int MAX_DEPTH = 20;
    private TranspositionTable tt;
    private HistoryHeuristic history;
    private KillerHeuristic killers;
    private long startTime;
    private long timeLimit;
    private int nodesSearched;
    private int maxDepthReached;
    private boolean abortSearch;

    public OptimizedAlphaBeta() {
        this.tt = new TranspositionTable();
        this.history = new HistoryHeuristic();
        this.killers = new KillerHeuristic(MAX_DEPTH);
    }

    public SearchResult search(Board board, int color, long timeLimitMs) {
        this.startTime = System.currentTimeMillis();
        this.timeLimit = timeLimitMs;
        this.nodesSearched = 0;
        this.maxDepthReached = 0;
        this.abortSearch = false;

        List<Move> moves = RuleValidator.generateAllMoves(board, color);
        if (moves.isEmpty()) return new SearchResult(null, RuleValidator.isInCheck(board, color) ? -INF + 1000 : 0);

        for (Move m : moves) {
            ChessPiece target = board.getPiece(m.getDestination());
            if (target != null && target.isRevealed() && target.getType() == ChessPiece.KING)
                return new SearchResult(m, INF - 1);
        }

        Move bestMove = null;
        int bestScore = -INF;
        long hash = ZobristHash.computeHash(board);
        Move ttBest = tt.getBestMove(hash);
        if (ttBest != null) orderMoveToFront(moves, ttBest);

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            if (abortSearch) break;
            int alpha = -INF, beta = INF;
            int currentBest = -INF;
            Move currentBestMove = null;

            for (int i = 0; i < moves.size(); i++) {
                if (abortSearch) break;
                Move move = moves.get(i);
                ChessPiece captured = board.executeMove(move);
                nodesSearched++;
                int score;
                int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
                if (RuleValidator.isCheckmate(board, oppColor)) score = INF - 1;
                else {
                    if (i == 0) score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, true);
                    else {
                        score = -alphaBeta(board, oppColor, depth - 1, -alpha - 1, -alpha, false);
                        if (score > alpha && score < beta)
                            score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, true);
                    }
                }
                board.undoMove(move, captured);
                if (score > currentBest) { currentBest = score; currentBestMove = move; }
                if (score > alpha) alpha = score;
                if (score > alpha) history.recordMove(move, color, depth);
            }

            if (!abortSearch) {
                maxDepthReached = depth;
                bestMove = currentBestMove;
                bestScore = currentBest;
                if (bestMove != null) tt.put(hash, depth, bestScore, TranspositionTable.EXACT, bestMove);
                if (Math.abs(bestScore) > INF - 1000) break;
                if (depth % 4 == 0) history.age();
            }
        }
        System.out.println("[AI] 搜索完成: 深度=" + maxDepthReached + ", 节点=" + nodesSearched + ", 分数=" + bestScore);
        return new SearchResult(bestMove, bestScore);
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

        List<Move> moves = RuleValidator.generateAllMoves(board, color);
        if (moves.isEmpty()) return RuleValidator.isInCheck(board, color) ? -INF + 1000 : 0;

        orderMoves(board, moves, color, depth, hash);
        int bestScore = -INF;
        Move bestMove = null;
        int alphaOrig = alpha;
        int searched = 0;

        for (Move move : moves) {
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
            if (searched == 0) score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, isPV);
            else {
                score = -alphaBeta(board, oppColor, depth - 1, -alpha - 1, -alpha, false);
                if (score > alpha && score < beta)
                    score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, true);
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
        if (depth <= 0) return EnhancedEvaluator.evaluate(board, color);
        int standPat = EnhancedEvaluator.evaluate(board, color);
        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        List<Move> captureMoves = new ArrayList<>();
        for (Move m : RuleValidator.generateAllMoves(board, color)) {
            if (board.getPiece(m.getDestination()) != null) captureMoves.add(m);
        }
        if (captureMoves.isEmpty()) return standPat;
        captureMoves.sort((a,b) -> {
            int vA = board.getPiece(a.getDestination()) != null ? board.getPiece(a.getDestination()).getValue() : 0;
            int vB = board.getPiece(b.getDestination()) != null ? board.getPiece(b.getDestination()).getValue() : 0;
            return vB - vA;
        });
        for (Move move : captureMoves) {
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
            if (target != null) sc += target.getValue() * 10 - (piece != null ? piece.getValue() : 0);
            if (move.isFlipOnly()) sc += 5000;
            else if (!piece.isRevealed()) sc += 3000;
            sc += killers.getKillerScore(move, depth);
            sc += history.getScore(move, color);
            sc += (8 - (Math.abs(dst[0]-4) + Math.abs(dst[1]-4))) * 3;
            if (target != null && target.isRevealed() && target.getType() == ChessPiece.KING) sc += 50000;
            scores.put(move, sc);
        }
        moves.sort((a,b) -> scores.getOrDefault(b,0) - scores.getOrDefault(a,0));
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