package com.jieqi.record;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReplayTimelineTest {

    @Test
    void recordInitial_onlyOnce() {
        Board board = new Board();
        ReplayTimeline timeline = new ReplayTimeline();
        timeline.recordInitial(board, ChessPiece.RED, Game.GameStatus.PLAYING);
        timeline.recordInitial(board, ChessPiece.BLACK, Game.GameStatus.PLAYING);
        assertEquals(1, timeline.size());
        assertEquals(0, timeline.getFrame(0).getStepIndex());
        assertNull(timeline.getFrame(0).getMove());
    }

    @Test
    void recordAfterMove_autoIncrementsStepIndex() {
        Board board = new Board();
        ReplayTimeline timeline = new ReplayTimeline();
        timeline.recordInitial(board, ChessPiece.RED, Game.GameStatus.PLAYING);
        Move move = new Move("a6", "a5");
        timeline.recordAfterMove(move, board, ChessPiece.BLACK, Game.GameStatus.PLAYING, null);
        assertEquals(2, timeline.size());
        assertEquals(1, timeline.getFrame(1).getStepIndex());
        assertEquals("a6", timeline.getFrame(1).getMove().getSource());
    }

    @Test
    void frame_returnsDefensiveCopy() {
        Board board = new Board();
        ReplayTimeline timeline = new ReplayTimeline();
        timeline.recordInitial(board, ChessPiece.RED, Game.GameStatus.PLAYING);
        ReplayFrame frame = timeline.getFrame(0);
        assertNotSame(board, frame.getBoardSnapshot());
        board.clearAllPieces();
        assertNotNull(timeline.getFrame(0).getBoardSnapshot().getPiece(9, 4));
    }

    @Test
    void getFrame_outOfBoundsReturnsNull() {
        ReplayTimeline timeline = new ReplayTimeline();
        assertNull(timeline.getFrame(0));
        assertNull(timeline.getFrame(-1));
    }
}
