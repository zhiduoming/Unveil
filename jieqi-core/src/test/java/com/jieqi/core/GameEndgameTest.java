package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEndgameTest {

    @Test
    void timeoutEndsGameForCurrentPlayer() {
        Game game = new Game("timeout-test");
        game.connectPlayer(ChessPiece.RED);
        game.connectPlayer(ChessPiece.BLACK);
        game.setTurnStartTime(System.currentTimeMillis() - 70_000L);

        String err = game.processMove(new Move("a0", "a1"), ChessPiece.RED);

        assertEquals("超时判负", err);
        assertEquals(Game.GameStatus.BLACK_WIN, game.getStatus());
        assertEquals(EndgameJudge.ProtocolReason.TIMEOUT, game.getGameOverReason());
    }
}
