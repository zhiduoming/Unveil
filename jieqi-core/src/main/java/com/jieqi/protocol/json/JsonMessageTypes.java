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
    public static final String START_AI_GAME = "startAiGame";
    public static final String START_AI_BATTLE = "startAiBattle";
    public static final String CANCEL_MATCH = "cancelMatch";
    public static final String CREATE_ROOM = "createRoom";
    public static final String JOIN_ROOM = "joinRoom";
    public static final String REQUEST_FIRST_HAND = "requestFirstHand";
    public static final String MOVE = "move";
    public static final String PING = "ping";
    public static final String RESIGN = "Resign";
    public static final String DRAW_OFFER = "drawOffer";
    public static final String DRAW_ACCEPT = "drawAccept";
    public static final String DRAW_DECLINE = "drawDecline";
    public static final String UNDO_OFFER = "undoOffer";
    public static final String UNDO_ACCEPT = "undoAccept";
    public static final String UNDO_DECLINE = "undoDecline";
    public static final String READY = "Ready";
    // 本组扩展（非老师协议）：对局结束后邀请再来一局
    public static final String REMATCH_REQUEST = "rematchRequest";
    public static final String REMATCH_DECLINE = "rematchDecline";
    // 本组扩展：主动离开房间（AI 对局收尾、用户中途返回大厅）
    public static final String LEAVE_ROOM = "leaveRoom";
    // 本组扩展：AI 对弈暂停 / 继续（仅 AI 对战/人机模式有效）
    public static final String PAUSE_GAME = "pauseGame";
    public static final String RESUME_GAME = "resumeGame";

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
    public static final String UNDO_OFFERED = "undoOffered";
    public static final String UNDO_DECLINED = "undoDeclined";
    public static final String UNDO_PERFORMED = "undoPerformed";
    // 本组扩展：rematch 流程通知
    public static final String REMATCH_OFFER = "rematchOffer";       // 转发对方邀请
    public static final String REMATCH_DECLINED = "rematchDeclined"; // 转发对方拒绝
    // 本组扩展：AI 对弈暂停状态变化通知
    public static final String GAME_PAUSED = "gamePaused";
    public static final String GAME_RESUMED = "gameResumed";
}
