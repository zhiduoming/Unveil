package com.jieqi.server;

import com.jieqi.core.Game;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 多盘对弈房间匹配：空 gameId 自动撮合，指定 gameId 加入或报错。
 */
public final class MatchmakingService {

    public enum JoinError {
        NONE,
        NOT_FOUND,
        ROOM_FULL,
        ALREADY_STARTED
    }

    public record JoinResult(Game game, JoinError error) {
        public boolean ok() {
            return error == JoinError.NONE && game != null;
        }
    }

    /**
     * @param games          当前全部对局
     * @param requestedGameId LOGIN 第三段；空串表示自动匹配
     * @param createNewGame  无可用房间时创建新对局（gameId 由调用方生成）
     */
    public JoinResult resolveJoin(
            Map<String, Game> games,
            String requestedGameId,
            Supplier<Game> createNewGame) {

        String id = requestedGameId == null ? "" : requestedGameId.trim();

        if ("new".equalsIgnoreCase(id) || "*".equals(id)) {
            Game created = createNewGame.get();
            games.put(created.getGameId(), created);
            logRoom(created, "新建房间（显式 new）");
            return new JoinResult(created, JoinError.NONE);
        }

        if (!id.isEmpty()) {
            Game existing = games.get(id);
            if (existing == null) {
                return new JoinResult(null, JoinError.NOT_FOUND);
            }
            JoinError err = validateJoinable(existing);
            if (err != JoinError.NONE) {
                return new JoinResult(null, err);
            }
            return new JoinResult(existing, JoinError.NONE);
        }

        Game waitingWithOne = null;
        Game emptyWaiting = null;
        for (Game g : games.values()) {
            if (g.getStatus() != Game.GameStatus.WAITING) {
                continue;
            }
            int n = g.connectedPlayerCount();
            if (n >= 2) {
                continue;
            }
            if (n == 1) {
                waitingWithOne = g;
                break;
            }
            if (n == 0 && emptyWaiting == null) {
                emptyWaiting = g;
            }
        }
        if (waitingWithOne != null) {
            logRoom(waitingWithOne, "自动匹配（并入 1/2 房间）");
            return new JoinResult(waitingWithOne, JoinError.NONE);
        }
        if (emptyWaiting != null) {
            logRoom(emptyWaiting, "自动匹配（空房间）");
            return new JoinResult(emptyWaiting, JoinError.NONE);
        }

        Game created = createNewGame.get();
        games.put(created.getGameId(), created);
        logRoom(created, "自动匹配（新建房间）");
        return new JoinResult(created, JoinError.NONE);
    }

    public static String newGameId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static JoinError validateJoinable(Game game) {
        if (game.getStatus() != Game.GameStatus.WAITING) {
            return JoinError.ALREADY_STARTED;
        }
        if (game.connectedPlayerCount() >= 2) {
            return JoinError.ROOM_FULL;
        }
        return JoinError.NONE;
    }

    private static void logRoom(Game game, String action) {
        System.out.println("[Match] " + action + " gameId=" + game.getGameId()
                + " players=" + game.connectedPlayerCount() + "/2"
                + " status=" + game.getStatus());
    }
}
