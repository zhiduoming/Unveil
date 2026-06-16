package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameBonusTimeTest {

    @Test
    void addBonusTimeMsExtendsCurrentTurnDeadline() {
        Game game = new Game("bonus-test");
        game.setStatus(Game.GameStatus.PLAYING);
        game.setTurnStartTime(System.currentTimeMillis() - 70_000L);
        assertTrue(game.isTimeout(), "应已超时");

        assertTrue(game.addBonusTimeMs(30_000L));
        assertFalse(game.isTimeout(), "加时后应未超时");
    }

    @Test
    void addBonusTimeMsIgnoredWhenNotPlaying() {
        Game game = new Game("bonus-idle");
        assertFalse(game.addBonusTimeMs(30_000L));
    }
}
