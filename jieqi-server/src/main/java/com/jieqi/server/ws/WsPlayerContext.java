package com.jieqi.server.ws;

import com.jieqi.core.ChessPiece;
import org.java_websocket.WebSocket;

/** 单个 WebSocket 连接上的玩家会话状态。 */
public final class WsPlayerContext {

    private final WebSocket connection;
    private String userId;
    private String nickname;
    private String roomId;
    private int color = -1;
    private boolean ready;
    private Boolean wannaFirst;

    public WsPlayerContext(WebSocket connection) {
        this.connection = connection;
    }

    public WebSocket connection() {
        return connection;
    }

    public String userId() {
        return userId;
    }

    public void setUser(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    public String nickname() {
        return nickname;
    }

    public String roomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int color() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isRed() {
        return color == ChessPiece.RED;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public Boolean wannaFirst() {
        return wannaFirst;
    }

    public void setWannaFirst(Boolean wannaFirst) {
        this.wannaFirst = wannaFirst;
    }

    public boolean isLoggedIn() {
        return userId != null;
    }
}
