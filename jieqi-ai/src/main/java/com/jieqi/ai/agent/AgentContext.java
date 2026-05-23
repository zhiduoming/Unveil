package com.jieqi.ai.agent;

import com.jieqi.core.Board;

/** 多 Agent 协作上下文。 */
public final class AgentContext {
    private final Board board;
    private final int color;
    private final long timeLimitMs;
    private int probabilityBias;

    public AgentContext(Board board, int color, long timeLimitMs) {
        this.board = board;
        this.color = color;
        this.timeLimitMs = timeLimitMs;
    }

    public Board getBoard() { return board; }
    public int getColor() { return color; }
    public long getTimeLimitMs() { return timeLimitMs; }

    public int getProbabilityBias() { return probabilityBias; }
    public void setProbabilityBias(int bias) { this.probabilityBias = bias; }

    public int pieceCount() {
        int n = 0;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.getPiece(r, c) != null) n++;
            }
        }
        return n;
    }
}
