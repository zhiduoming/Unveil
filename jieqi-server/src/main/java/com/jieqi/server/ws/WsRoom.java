package com.jieqi.server.ws;

import com.jieqi.core.Game;

/** WebSocket 对局房间：两名玩家 + 共享 Game。 */
public final class WsRoom {

    private final String roomId;
    private final Game game;
    private WsPlayerContext red;
    private WsPlayerContext black;
    private boolean redReady;
    private boolean blackReady;
    private Boolean redWannaFirst;
    private Boolean blackWannaFirst;
    private final long matchedAtMs;
    private boolean started;

    public WsRoom(String roomId, Game game) {
        this.roomId = roomId;
        this.game = game;
        this.matchedAtMs = System.currentTimeMillis();
    }

    public String roomId() {
        return roomId;
    }

    public Game game() {
        return game;
    }

    public WsPlayerContext red() {
        return red;
    }

    public WsPlayerContext black() {
        return black;
    }

    public void bindPlayer(WsPlayerContext ctx, int color) {
        ctx.setRoomId(roomId);
        ctx.setColor(color);
        if (color == com.jieqi.core.ChessPiece.RED) {
            red = ctx;
            game.setRedPlayerName(ctx.nickname());
            if (!game.isRedConnected()) {
                game.connectPlayer(com.jieqi.core.ChessPiece.RED);
            }
        } else {
            black = ctx;
            game.setBlackPlayerName(ctx.nickname());
            if (!game.isBlackConnected()) {
                game.connectPlayer(com.jieqi.core.ChessPiece.BLACK);
            }
        }
        if (!started && game.connectedPlayerCount() == 2) {
            game.setStatus(Game.GameStatus.WAITING);
        }
    }

    public void swapColors() {
        WsPlayerContext r = red;
        WsPlayerContext b = black;
        if (r == null || b == null) {
            return;
        }
        r.setColor(com.jieqi.core.ChessPiece.BLACK);
        b.setColor(com.jieqi.core.ChessPiece.RED);
        red = b;
        black = r;
        String redName = game.getRedPlayerName();
        game.setRedPlayerName(game.getBlackPlayerName());
        game.setBlackPlayerName(redName);
    }

    public WsPlayerContext opponentOf(WsPlayerContext ctx) {
        if (ctx == red) {
            return black;
        }
        if (ctx == black) {
            return red;
        }
        return null;
    }

    public boolean bothConnected() {
        return red != null && black != null;
    }

    public boolean isRedReady() {
        return redReady;
    }

    public void setRedReady(boolean redReady) {
        this.redReady = redReady;
    }

    public boolean isBlackReady() {
        return blackReady;
    }

    public void setBlackReady(boolean blackReady) {
        this.blackReady = blackReady;
    }

    public void setReady(int color, boolean ready) {
        if (color == com.jieqi.core.ChessPiece.RED) {
            redReady = ready;
        } else {
            blackReady = ready;
        }
    }

    public boolean bothReady() {
        return redReady && blackReady;
    }

    public Boolean redWannaFirst() {
        return redWannaFirst;
    }

    public void setRedWannaFirst(Boolean redWannaFirst) {
        this.redWannaFirst = redWannaFirst;
    }

    public Boolean blackWannaFirst() {
        return blackWannaFirst;
    }

    public void setBlackWannaFirst(Boolean blackWannaFirst) {
        this.blackWannaFirst = blackWannaFirst;
    }

    public void setWannaFirst(int color, boolean wanna) {
        if (color == com.jieqi.core.ChessPiece.RED) {
            redWannaFirst = wanna;
        } else {
            blackWannaFirst = wanna;
        }
    }

    public long matchedAtMs() {
        return matchedAtMs;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
