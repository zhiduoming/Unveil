package com.jieqi.ai;

import com.jieqi.core.Move;

public class KillerHeuristic {
    private Move[][] killers;
    private int maxDepth;

    public KillerHeuristic(int maxDepth) {
        this.maxDepth = maxDepth;
        this.killers = new Move[maxDepth][2];
    }

    public void addKiller(Move move, int depth) {
        if (depth >= maxDepth) return;
        if (isKiller(move, depth)) return;
        killers[depth][1] = killers[depth][0];
        killers[depth][0] = move;
    }

    public boolean isKiller(Move move, int depth) {
        if (depth >= maxDepth) return false;
        for (int i = 0; i < 2; i++) {
            if (killers[depth][i] != null &&
                killers[depth][i].getSource().equals(move.getSource()) &&
                killers[depth][i].getDestination().equals(move.getDestination()))
                return true;
        }
        return false;
    }

    public int getKillerScore(Move move, int depth) {
        return isKiller(move, depth) ? 9000 - depth * 100 : 0;
    }

    public void clear() {
        killers = new Move[maxDepth][2];
    }
}