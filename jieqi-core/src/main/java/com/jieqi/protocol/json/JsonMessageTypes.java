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
    public static final String DRAW_OFFER = "drawOffer";
    public static final String DRAW_ACCEPT = "drawAccept";
    public static final String DRAW_DECLINE = "drawDecline";
    public static final String READY = "Ready";
    // 本组扩展（非老师协议）：对局结束后邀请再来一局
    public static final String REMATCH_REQUEST = "rematchRequest";
    public static final String REMATCH_DECLINE = "rematchDecline";

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
    public static final String DRAW_OFFERED = "drawOffered";
    public static final String DRAW_DECLINED = "drawDeclined";
    // 本组扩展：rematch 流程通知
    public static final String REMATCH_OFFER = "rematchOffer";       // 转发对方邀请
    public static final String REMATCH_DECLINED = "rematchDeclined"; // 转发对方拒绝
}
