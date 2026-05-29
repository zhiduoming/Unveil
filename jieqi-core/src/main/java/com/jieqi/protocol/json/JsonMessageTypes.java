package com.jieqi.protocol.json;

/**
 * 老师 2026 大作业公共接口 — JSON messageType 常量。
 * 权威文档：docs/INTERFACE.typ v3.0 正文。
 */
public final class JsonMessageTypes {

    private JsonMessageTypes() {}

    // C → S
    public static final String LOGIN = "Login";
    public static final String REGISTER = "register";
    public static final String START_MATCH = "startMatch";
    public static final String CANCEL_MATCH = "cancelMatch";
    public static final String REQUEST_FIRST_HAND = "requestFirstHand";
    public static final String MOVE = "move";
    public static final String PING = "ping";
    public static final String RESIGN = "Resign";
    public static final String READY = "Ready";

    // S → C
    public static final String LOGIN_RESULT = "loginResult";
    public static final String MATCH_SUCCESS = "matchSuccess";
    public static final String GAME_START = "gameStart";
    public static final String MOVE_RESULT = "moveResult";
    public static final String TIMEOUT = "timeout";
    public static final String GAME_OVER = "gameOver";
    public static final String PONG = "pong";
    public static final String ERROR = "error";
    public static final String ROOM_INFO = "roomInfo";
}
