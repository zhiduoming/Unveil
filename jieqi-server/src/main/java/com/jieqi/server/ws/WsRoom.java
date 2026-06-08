package com.jieqi.server.ws;

import com.jieqi.core.Game;

/** WebSocket 对局房间：两名玩家 + 共享 Game。 */
public final class WsRoom {

    private final String roomId;
    private Game game;                 // 改为可变：rematch 时重新构造一个新的 Game
    private WsPlayerContext red;
    private WsPlayerContext black;
    private boolean aiOpponent;
    private int aiColor = -1;
    private String aiUserId = "AI";
    private String aiNickname = "AI";
    private boolean aiBattle;
    private WsPlayerContext observer;
    private String aiRedUserId = "ai_red";
    private String aiRedNickname = "AI 红方";
    private String aiBlackUserId = "ai_black";
    private String aiBlackNickname = "AI 黑方";
    private boolean redReady;
    private boolean blackReady;
    private Boolean redWannaFirst;
    private Boolean blackWannaFirst;
    private final long matchedAtMs;
    private boolean started;

    // ── Draw offer（对局中提和） ──
    private int drawOfferedByColor = -1; // -1=无提和，红/黑=当前提和发起方

    // ── Rematch（本组扩展：对局结束后双方可邀请再来一局） ──
    private boolean finished;          // 对局已结束（保留 room 给 rematch 用）
    private long finishedAtMs;         // 结束时刻，用于超时清理
    private boolean redRematchAsked;   // 红方已请求 rematch
    private boolean blackRematchAsked; // 黑方已请求 rematch

    // ── Pause（本组扩展：AI 对弈暂停） ──
    private boolean paused;            // true 时 AI 调度线程不下一步

    public WsRoom(String roomId, Game game) {
        this.roomId = roomId;
        this.game = game;
        this.matchedAtMs = System.currentTimeMillis();
    }

    /** rematch 时替换 Game 实例。 */
    public void replaceGame(Game game) {
        this.game = game;
    }

    public boolean isFinished() { return finished; }
    public long finishedAtMs() { return finishedAtMs; }
    public void markFinished() {
        this.finished = true;
        this.finishedAtMs = System.currentTimeMillis();
        this.redRematchAsked = false;
        this.blackRematchAsked = false;
    }

    public boolean isRedRematchAsked() { return redRematchAsked; }
    public boolean isBlackRematchAsked() { return blackRematchAsked; }
    public void setRematchAsked(int color, boolean asked) {
        if (color == com.jieqi.core.ChessPiece.RED) redRematchAsked = asked;
        else blackRematchAsked = asked;
    }
    public boolean bothRematchAsked() { return redRematchAsked && blackRematchAsked; }

    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }

    public int drawOfferedByColor() { return drawOfferedByColor; }
    public void setDrawOfferedByColor(int color) { this.drawOfferedByColor = color; }
    public void clearDrawOffer() { this.drawOfferedByColor = -1; }

    /** rematch 同意后清状态，准备进入新局。 */
    public void resetForRematch() {
        this.finished = false;
        this.finishedAtMs = 0;
        this.redRematchAsked = false;
        this.blackRematchAsked = false;
        this.redReady = false;
        this.blackReady = false;
        this.redWannaFirst = null;
        this.blackWannaFirst = null;
        this.started = false;
        this.drawOfferedByColor = -1;
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

    public boolean hasAiOpponent() { return aiOpponent; }
    public int aiColor() { return aiColor; }
    public String aiUserId() { return aiUserId; }
    public String aiNickname() { return aiNickname; }
    public boolean isAiBattle() { return aiBattle; }
    public WsPlayerContext observer() { return observer; }
    public boolean isObserver(WsPlayerContext ctx) { return observer == ctx; }
    public String aiUserIdForColor(int color) {
        if (aiBattle) {
            return color == com.jieqi.core.ChessPiece.RED ? aiRedUserId : aiBlackUserId;
        }
        return aiOpponent && aiColor == color ? aiUserId : "";
    }
    public String aiNicknameForColor(int color) {
        if (aiBattle) {
            return color == com.jieqi.core.ChessPiece.RED ? aiRedNickname : aiBlackNickname;
        }
        return aiOpponent && aiColor == color ? aiNickname : "";
    }

    public void enableAiOpponent(int color, String userId, String nickname) {
        this.aiOpponent = true;
        this.aiColor = color;
        this.aiUserId = userId;
        this.aiNickname = nickname;
        if (color == com.jieqi.core.ChessPiece.RED) {
            game.setRedPlayerName(nickname);
            if (!game.isRedConnected()) {
                game.connectPlayer(com.jieqi.core.ChessPiece.RED);
            }
        } else {
            game.setBlackPlayerName(nickname);
            if (!game.isBlackConnected()) {
                game.connectPlayer(com.jieqi.core.ChessPiece.BLACK);
            }
        }
        if (!started && game.connectedPlayerCount() == 2) {
            game.setStatus(Game.GameStatus.WAITING);
        }
    }

    public void enableAiBattle(WsPlayerContext observer, String redUserId, String redNickname,
                               String blackUserId, String blackNickname) {
        this.aiBattle = true;
        this.observer = observer;
        observer.setRoomId(roomId);
        observer.setColor(-1);
        this.aiRedUserId = redUserId;
        this.aiRedNickname = redNickname;
        this.aiBlackUserId = blackUserId;
        this.aiBlackNickname = blackNickname;
        game.setRedPlayerName(redNickname);
        game.setBlackPlayerName(blackNickname);
        if (!game.isRedConnected()) {
            game.connectPlayer(com.jieqi.core.ChessPiece.RED);
        }
        if (!game.isBlackConnected()) {
            game.connectPlayer(com.jieqi.core.ChessPiece.BLACK);
        }
        if (!started && game.connectedPlayerCount() == 2) {
            game.setStatus(Game.GameStatus.WAITING);
        }
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
        if (aiOpponent && ctx.color() == (aiColor == com.jieqi.core.ChessPiece.RED
                ? com.jieqi.core.ChessPiece.BLACK : com.jieqi.core.ChessPiece.RED)) {
            return null;
        }
        if (ctx == red) {
            return black;
        }
        if (ctx == black) {
            return red;
        }
        return null;
    }

    public boolean bothConnected() {
        if (aiBattle) {
            return observer != null;
        }
        if (aiOpponent) {
            return aiColor == com.jieqi.core.ChessPiece.RED ? black != null : red != null;
        }
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
        if (aiBattle) {
            return observer != null;
        }
        if (aiOpponent) {
            return aiColor == com.jieqi.core.ChessPiece.RED ? blackReady : redReady;
        }
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
