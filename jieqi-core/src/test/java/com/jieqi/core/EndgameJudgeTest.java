package com.jieqi.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EndgameJudgeTest {

    @Test
    void repetitionDrawOnSixthIdenticalPosition() {
        Board board = new Board();
        Map<String, Integer> rep = new HashMap<>();
        String hash = "test-position-hash";
        EndgameJudge.Verdict last = null;
        for (int i = 0; i < 6; i++) {
            last = EndgameJudge.checkAfterMove(board, ChessPiece.RED, null, rep, hash);
        }
        assertNotNull(last);
        assertEquals(Game.GameStatus.DRAW, last.status());
        assertEquals(EndgameJudge.ProtocolReason.REPETITION_DRAW, last.reasonCode());
        assertEquals(6, rep.get(hash));
    }
}
