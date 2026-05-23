package com.jieqi.server;

import com.jieqi.core.Game;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatchmakingServiceTest {

    private final MatchmakingService service = new MatchmakingService();

    @Test
    void autoMatchPrefersRoomWithOnePlayer() {
        Map<String, Game> games = new HashMap<>();
        Game partial = new Game("room-a");
        partial.connectPlayer(0);
        games.put("room-a", partial);
        Game empty = new Game("room-b");
        games.put("room-b", empty);

        MatchmakingService.JoinResult r = service.resolveJoin(games, "", () -> new Game("new-id"));
        assertTrue(r.ok());
        assertEquals("room-a", r.game().getGameId());
    }

    @Test
    void specifiedMissingRoomReturnsNotFound() {
        Map<String, Game> games = new HashMap<>();
        MatchmakingService.JoinResult r = service.resolveJoin(games, "missing", () -> new Game("x"));
        assertEquals(MatchmakingService.JoinError.NOT_FOUND, r.error());
    }

    @Test
    void playingRoomReturnsAlreadyStarted() {
        Map<String, Game> games = new HashMap<>();
        Game g = new Game("g1");
        g.connectPlayer(0);
        g.connectPlayer(1);
        games.put("g1", g);
        assertEquals(Game.GameStatus.PLAYING, g.getStatus());

        MatchmakingService.JoinResult r = service.resolveJoin(games, "g1", () -> new Game("x"));
        assertEquals(MatchmakingService.JoinError.ALREADY_STARTED, r.error());
    }

    @Test
    void newKeywordAlwaysCreatesRoom() {
        Map<String, Game> games = new HashMap<>();
        MatchmakingService.JoinResult r = service.resolveJoin(games, "new", () -> new Game("fresh"));
        assertTrue(r.ok());
        assertEquals("fresh", r.game().getGameId());
        assertTrue(games.containsKey("fresh"));
    }
}
