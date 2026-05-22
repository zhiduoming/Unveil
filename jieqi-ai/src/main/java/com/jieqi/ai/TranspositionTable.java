package com.jieqi.ai;

import com.jieqi.core.Move;

public class TranspositionTable {
    public static final int EXACT = 0;
    public static final int LOWER = 1;
    public static final int UPPER = 2;

    private Entry[] entries;
    private int size;

    public TranspositionTable(int size) {
        this.size = size;
        this.entries = new Entry[size];
    }

    public TranspositionTable() {
        this(1 << 20);
    }

    public void put(long hash, int depth, int score, int flag, Move bestMove) {
        int index = (int)(hash & (size - 1));
        Entry existing = entries[index];
        if (existing == null || existing.depth <= depth || existing.hash != hash) {
            Entry entry = new Entry();
            entry.hash = hash;
            entry.depth = depth;
            entry.score = score;
            entry.flag = flag;
            entry.bestMove = bestMove;
            entries[index] = entry;
        }
    }

    public Entry get(long hash) {
        int index = (int)(hash & (size - 1));
        Entry e = entries[index];
        return (e != null && e.hash == hash) ? e : null;
    }

    public Move getBestMove(long hash) {
        Entry e = get(hash);
        return e != null ? e.bestMove : null;
    }

    public void clear() {
        for (int i = 0; i < size; i++) entries[i] = null;
    }

    public static class Entry {
        public long hash;
        public int depth;
        public int score;
        public int flag;
        public Move bestMove;
        public Move getBestMove() { return bestMove; }
        public int getDepth() { return depth; }
        public int getScore() { return score; }
        public int getFlag() { return flag; }
    }
}