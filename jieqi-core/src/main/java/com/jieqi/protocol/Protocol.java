package com.jieqi.protocol;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;

import java.io.UnsupportedEncodingException;

/**
 * TCP 文本帧协议 v2.0（本组可选扩展，见 docs/INTERFACE.typ 附录 B）。
 * 组间互操作正文权威：WebSocket + JSON v3.0 — com.jieqi.protocol.json.*
 */
public class Protocol {

    // ── 消息类型 ───────────────────────────────────────────
    public static final int MSG_LOGIN        = 1;
    public static final int MSG_MOVE         = 2;
    public static final int MSG_GAME_STATE   = 3;
    public static final int MSG_ERROR        = 4;
    public static final int MSG_QUIT         = 5;
    public static final int MSG_GAME_OVER    = 6;
    public static final int MSG_BOARD_STATE  = 7;
    public static final int MSG_DRAW_REQUEST = 8;
    public static final int MSG_RESIGN       = 9;
    public static final int MSG_CHAT         = 10;

    // ── 游戏结束原因码 ──────────────────────────────────────
    public static final int REASON_CHECKMATE        = 0;
    public static final int REASON_STALEMATE        = 1;
    public static final int REASON_TIMEOUT          = 2;
    public static final int REASON_RESIGN           = 3;
    public static final int REASON_DISCONNECT       = 4;
    public static final int REASON_KING_CAPTURED    = 5;
    public static final int REASON_NO_CAPTURE_DRAW  = 6;
    public static final int REASON_REPETITION_LOSS  = 7;
    public static final int REASON_REPETITION_DRAW  = 8;
    public static final int REASON_AGREED_DRAW      = 9;

    // ── 错误码 ────────────────────────────────────────────
    public static final int ERR_UNKNOWN           = 100;
    public static final int ERR_ILLEGAL_MOVE      = 101;
    public static final int ERR_PATH_BLOCKED      = 102;
    public static final int ERR_SAME_COLOR        = 103;
    public static final int ERR_KNIGHT_LEG        = 104;
    public static final int ERR_BISHOP_EYE        = 105;
    public static final int ERR_SELF_CHECK        = 106;
    public static final int ERR_NOT_YOUR_TURN     = 107;
    public static final int ERR_GAME_NOT_PLAYING  = 108;
    public static final int ERR_ALREADY_REVEALED  = 109;
    public static final int ERR_NO_PIECE_AT_SRC   = 110;
    public static final int ERR_MALFORMED_MSG     = 111;
    public static final int ERR_DUPLICATE_LOGIN   = 112;
    public static final int ERR_ROOM_NOT_FOUND    = 200;
    public static final int ERR_ROOM_FULL         = 201;
    public static final int ERR_COLOR_TAKEN       = 202;

    // ── 常量 ──────────────────────────────────────────────
    public static final long STEP_TIME_LIMIT_MS = 60_000L;
    public static final long NETWORK_GRACE_MS   =  5_000L;
    public static final long TIMEOUT_THRESHOLD  = STEP_TIME_LIMIT_MS + NETWORK_GRACE_MS; // 65000

    // ── 帧构建 / 解析 ──────────────────────────────────────

    /**
     * 构建完整消息帧：msgType|payloadByteLength|payload
     * 发送时附加 \n。
     */
    public static String buildMessage(int msgType, String data) {
        try {
            int len = data.getBytes("UTF-8").length;
            return msgType + "|" + len + "|" + data;
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is guaranteed on all JVMs
            throw new RuntimeException(e);
        }
    }

    /**
     * 从到达的一行（不含 \n）中提取 msgType。
     */
    public static int parseMsgType(String line) {
        return Integer.parseInt(line.split("\\|")[0]);
    }

    /**
     * 从到达的一行（不含 \n）中提取 payload。
     * 同时校验 payloadByteLength，若不一致返回 null。
     */
    public static String parsePayload(String line) {
        String[] parts = line.split("\\|", 3);
        if (parts.length < 3) return "";
        int declaredLen;
        try {
            declaredLen = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        String payload = parts[2];
        try {
            if (payload.getBytes("UTF-8").length != declaredLen) {
                return null; // 帧损坏
            }
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return payload;
    }

    /**
     * @deprecated 使用 parsePayload(String) 替代，以启用长度校验。
     */
    @Deprecated
    public static String parseData(String msg) {
        String[] parts = msg.split("\\|", 3);
        return parts.length == 3 ? parts[2] : "";
    }

    // ── 业务消息构建器 ──────────────────────────────────────

    public static String buildLoginMsg(int color, String playerName) {
        return buildLoginMsg(color, playerName, "");
    }

    public static String buildLoginMsg(int color, String playerName, String gameId) {
        return buildMessage(MSG_LOGIN, color + "|" + playerName + "|" + (gameId != null ? gameId : ""));
    }

    public static String buildLoginAck(String gameId, int assignedColor, String status) {
        return buildMessage(MSG_GAME_STATE, "LOGIN_ACK|" + gameId + "|" + assignedColor + "|" + status);
    }

    public static String buildGameStart(int firstMoveColor) {
        return buildMessage(MSG_GAME_STATE, "GAME_START|" + firstMoveColor);
    }

    public static String buildTurnChange(int currentTurnColor) {
        return buildMessage(MSG_GAME_STATE, "TURN_CHANGE|" + currentTurnColor);
    }

    public static String buildPause(String reason) {
        return buildMessage(MSG_GAME_STATE, "PAUSE|" + reason);
    }

    public static String buildResume() {
        return buildMessage(MSG_GAME_STATE, "RESUME|");
    }

    public static String buildErrorMsg(int errorCode, String errorMessage) {
        return buildMessage(MSG_ERROR, errorCode + "|" + errorMessage);
    }

    /** @deprecated 使用 buildErrorMsg(int, String) 替代。 */
    @Deprecated
    public static String buildErrorMsg(String errorMessage) {
        return buildErrorMsg(ERR_UNKNOWN, errorMessage);
    }

    public static String buildGameOverMsg(int winner, int reasonCode) {
        String reasonDesc = getReasonDescription(reasonCode);
        return buildMessage(MSG_GAME_OVER, winner + "|" + reasonCode + "|" + reasonDesc);
    }

    /** @deprecated 使用 buildGameOverMsg(int, int) 替代。 */
    @Deprecated
    public static String buildGameOverMsg(int winner) {
        return buildGameOverMsg(winner, winner == -1 ? REASON_NO_CAPTURE_DRAW : REASON_CHECKMATE);
    }

    public static String buildDrawOffer() {
        return buildMessage(MSG_DRAW_REQUEST, "OFFER");
    }

    public static String buildDrawResponse(boolean accept) {
        return buildMessage(MSG_DRAW_REQUEST, accept ? "ACCEPT" : "DECLINE");
    }

    public static String buildResignMsg() {
        return buildMessage(MSG_RESIGN, "");
    }

    public static String buildResignNotify(int color) {
        return buildMessage(MSG_RESIGN, String.valueOf(color));
    }

    public static String buildChatMsg(int playerColor, String playerName, String message) {
        return buildMessage(MSG_CHAT, playerColor + "|" + playerName + "|" + message);
    }

    // ── Move 序列化 ─────────────────────────────────────────

    public static String serializeMove(Move move) {
        return move.getSource() + "|" + move.getDestination() + "|" +
               (move.getType() != null ? String.valueOf(move.getType()) : "") + "|" +
               move.getTurnStartTime() + "|" + (move.isFlipOnly() ? "1" : "0");
    }

    public static Move deserializeMove(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 3) return null;
        Move move = new Move(parts[0], parts[1]);
        if (!parts[2].isEmpty()) move.setType(Integer.parseInt(parts[2]));
        if (parts.length >= 4 && !parts[3].isEmpty()) move.setTurnStartTime(Long.parseLong(parts[3]));
        if (parts.length >= 5) move.setFlipOnly(parts[4].equals("1"));
        return move;
    }

    // ── Board 序列化 ────────────────────────────────────────

    /**
     * 按 INTERFACE.md §4.7 格式序列化棋盘。
     * 格式：currentTurn|row0|row1|...|row9
     * 行内 cell 以 {@code ,} 分隔，行间以 {@code |} 分隔（共 11 段：turn + 10 行）。
     */
    public static String buildBoardState(Board board, int currentTurn) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentTurn).append("|");
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null) {
                    sb.append(".");
                } else {
                    sb.append(p.getColor());
                    if (p.isRevealed()) {
                        sb.append(p.getType());
                    } else {
                        sb.append("?");
                    }
                }
                if (c < 8) sb.append(",");
            }
            if (r < 9) sb.append("|");
        }
        return buildMessage(MSG_BOARD_STATE, sb.toString());
    }

    /**
     * 从 BOARD_STATE payload 解析 currentTurn。
     */
    public static int parseCurrentTurnFromBoardState(String payload) {
        return Integer.parseInt(payload.split("\\|")[0]);
    }

    /**
     * 将 BOARD_STATE payload 应用到棋盘，返回当前走子方；失败返回 -1。
     */
    public static int applyBoardState(Board board, String payload) {
        return board.syncFromBoardStatePayload(payload);
    }

    // ── 辅助 ────────────────────────────────────────────────

    public static String getReasonDescription(int reasonCode) {
        switch (reasonCode) {
            case REASON_CHECKMATE:        return "将死";
            case REASON_STALEMATE:        return "困毙";
            case REASON_TIMEOUT:          return "超时";
            case REASON_RESIGN:           return "认输";
            case REASON_DISCONNECT:       return "对手断线";
            case REASON_KING_CAPTURED:    return "吃将获胜";
            case REASON_NO_CAPTURE_DRAW:  return "40回合无吃子和棋";
            case REASON_REPETITION_LOSS:  return "长将/长捉判负";
            case REASON_REPETITION_DRAW:  return "兵卒长捉和";
            case REASON_AGREED_DRAW:      return "协议和棋";
            default:                      return "未知原因";
        }
    }

    public static String getColorName(int color) {
        return color == ChessPiece.RED ? "红方" : "黑方";
    }
}
