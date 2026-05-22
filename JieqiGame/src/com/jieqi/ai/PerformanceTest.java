package com.jieqi.ai;

import com.jieqi.core.*;

public class PerformanceTest {
    public static void main(String[] args) {
        System.out.println("====== AI性能测试 ======\n");
        testSearchPerformance();
        testTacticalAwareness();
    }

    private static void testSearchPerformance() {
        Board board = new Board();
        OptimizedAlphaBeta ai = new OptimizedAlphaBeta();
        System.out.println("1. 搜索性能测试（初始局面）");
        System.out.println("----------------------------------------");
        for (int timeMs : new int[]{1000, 2000, 5000, 10000}) {
            ai.clearHeuristics();
            long start = System.currentTimeMillis();
            OptimizedAlphaBeta.SearchResult result = ai.search(board, ChessPiece.RED, timeMs);
            long elapsed = System.currentTimeMillis() - start;
            System.out.printf("时间限制:%5dms | 实际:%5dms | 深度:%2d | 节点:%8d | 分数:%+8d\n",
                timeMs, elapsed, ai.getMaxDepthReached(), ai.getNodesSearched(), result.score);
        }
        System.out.println();
    }

    private static void testTacticalAwareness() {
        Board board = new Board();
        OptimizedAlphaBeta ai = new OptimizedAlphaBeta();
        long start = System.currentTimeMillis();
        OptimizedAlphaBeta.SearchResult result = ai.search(board, ChessPiece.RED, 3000);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("2. 战术意识测试");
        System.out.println("  推荐走法: " + (result.bestMove != null ? result.bestMove : "无"));
        System.out.println("  评估分数: " + result.score);
        System.out.println("  搜索深度: " + ai.getMaxDepthReached());
        System.out.println("  搜索节点: " + ai.getNodesSearched());
        System.out.println("  搜索耗时: " + elapsed + "ms");
    }
}