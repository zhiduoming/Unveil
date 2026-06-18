package com.jieqi.record;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 一局棋的复盘时间线（内存权威数据源）。 */
public class ReplayTimeline {

    private final List<ReplayFrame> frames = new ArrayList<>();

    public void recordInitial(Board board, int currentTurn, Game.GameStatus status) {
        if (!frames.isEmpty()) {
            return;
        }
        frames.add(new ReplayFrame(0, null, board, currentTurn, status,
                System.currentTimeMillis(), null));
    }

    public void recordAfterMove(Move move, Board board, int currentTurn,
                                Game.GameStatus status, ChessPiece captured) {
        int stepIndex = frames.size();
        frames.add(new ReplayFrame(stepIndex, move, board, currentTurn, status,
                System.currentTimeMillis(), captured));
    }

    public ReplayFrame getFrame(int index) {
        if (index < 0 || index >= frames.size()) {
            return null;
        }
        return frames.get(index);
    }

    public int size() {
        return frames.size();
    }

    public List<ReplayFrame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }
}
