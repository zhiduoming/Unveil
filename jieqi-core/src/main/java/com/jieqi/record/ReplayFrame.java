package com.jieqi.record;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;

/**
 * 复盘单帧：含棋盘快照与该局面的元数据（防御性拷贝，不可变）。
 */
public final class ReplayFrame {

    private final int stepIndex;
    private final Move move;
    private final Board boardSnapshot;
    private final int currentTurn;
    private final Game.GameStatus status;
    private final long timestamp;
    private final ChessPiece captured;

    public ReplayFrame(int stepIndex, Move move, Board board, int currentTurn,
                       Game.GameStatus status, long timestamp, ChessPiece captured) {
        this.stepIndex = stepIndex;
        this.move = move == null ? null : copyMove(move);
        this.boardSnapshot = new Board(board);
        this.currentTurn = currentTurn;
        this.status = status;
        this.timestamp = timestamp;
        this.captured = captured == null ? null : new ChessPiece(captured);
    }

    private static Move copyMove(Move src) {
        Move m = new Move(src.getSource(), src.getDestination());
        if (src.getType() != null) {
            m.setType(src.getType());
        }
        m.setFlipOnly(src.isFlipOnly());
        m.setTurnStartTime(src.getTurnStartTime());
        m.setClientTimestamp(src.getClientTimestamp());
        m.setServerTimestamp(src.getServerTimestamp());
        if (src.getRevealedType() >= 0) {
            m.setRevealedType(src.getRevealedType());
        }
        return m;
    }

    public int getStepIndex() { return stepIndex; }

    public Move getMove() {
        return move == null ? null : copyMove(move);
    }

    public Board getBoardSnapshot() {
        return new Board(boardSnapshot);
    }

    public int getCurrentTurn() { return currentTurn; }

    public Game.GameStatus getStatus() { return status; }

    public long getTimestamp() { return timestamp; }

    public ChessPiece getCaptured() {
        return captured == null ? null : new ChessPiece(captured);
    }
}
