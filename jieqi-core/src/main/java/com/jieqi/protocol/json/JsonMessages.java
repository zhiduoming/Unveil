package com.jieqi.protocol.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;

/** 老师公共接口 JSON 消息构建与解析。 */
public final class JsonMessages {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private JsonMessages() {}

    public static String toJson(JsonObject obj) {
        return GSON.toJson(obj);
    }

    public static JsonObject parse(String raw) {
        return JsonParser.parseString(raw).getAsJsonObject();
    }

    public static String messageType(JsonObject obj) {
        return obj.get("messageType").getAsString();
    }

    // ── 构建 S→C ──────────────────────────────────────────

    public static JsonObject loginResult(boolean success, String message, String userId) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.LOGIN_RESULT);
        o.addProperty("success", success);
        o.addProperty("message", message);
        if (success && userId != null) {
            o.addProperty("userId", userId);
        }
        return o;
    }

    public static JsonObject matchSuccess(String roomId, String opponentId, String opponentNickname) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.MATCH_SUCCESS);
        o.addProperty("roomId", roomId);
        o.addProperty("opponentId", opponentId);
        o.addProperty("opponentNickname", opponentNickname);
        return o;
    }

    public static JsonObject gameStart(String redPlayerId, String blackPlayerId,
                                       String yourColor, boolean firstHand, Board board) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.GAME_START);
        o.addProperty("redPlayerId", redPlayerId);
        o.addProperty("blackPlayerId", blackPlayerId);
        o.addProperty("yourColor", yourColor);
        o.addProperty("firstHand", firstHand);
        o.add("initialBoard", BoardJsonMapper.toInitialBoard(board));
        return o;
    }

    public static JsonObject moveResult(boolean success, Move move, boolean valid,
                                        String flipResult) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.MOVE_RESULT);
        o.addProperty("success", success);
        o.addProperty("valid", valid);
        if (move != null) {
            o.add("move", moveToJson(move));
        }
        if (flipResult != null) {
            o.addProperty("flipResult", flipResult);
        }
        return o;
    }

    public static JsonObject timeout(String loserId, String winnerId) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.TIMEOUT);
        o.addProperty("loserId", loserId);
        o.addProperty("winnerId", winnerId);
        o.addProperty("reason", "timeout");
        return o;
    }

    public static JsonObject gameOver(String winnerColor, String reason, String winnerId) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.GAME_OVER);
        o.addProperty("winner", winnerColor);
        o.addProperty("reason", reason);
        o.addProperty("winnerId", winnerId);
        return o;
    }

    public static JsonObject pong(long timestamp) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.PONG);
        o.addProperty("timestamp", timestamp);
        return o;
    }

    public static JsonObject error(int code, String message) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.ERROR);
        o.addProperty("code", code);
        o.addProperty("message", message);
        return o;
    }

    public static JsonObject roomInfo(boolean opponentReady) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.ROOM_INFO);
        o.addProperty("opponentReady", opponentReady);
        return o;
    }

    public static JsonObject drawOffered(String fromUserId) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.DRAW_OFFERED);
        o.addProperty("fromUserId", fromUserId);
        return o;
    }

    public static JsonObject drawDeclined(String fromUserId) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.DRAW_DECLINED);
        o.addProperty("fromUserId", fromUserId);
        return o;
    }

    /** 转发对方的"再来一局"邀请。 */
    public static JsonObject rematchOffer(String fromUserId) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.REMATCH_OFFER);
        o.addProperty("fromUserId", fromUserId);
        return o;
    }

    /** 转发对方的"拒绝再来一局"。 */
    public static JsonObject rematchDeclined(String fromUserId) {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.REMATCH_DECLINED);
        o.addProperty("fromUserId", fromUserId);
        return o;
    }

    // ── 解析 C→S ──────────────────────────────────────────

    public static Move parseMove(JsonObject obj) {
        String fromX = obj.get("fromX").getAsString();
        int fromY = obj.get("fromY").getAsInt();
        String toX = obj.get("toX").getAsString();
        int toY = obj.get("toY").getAsInt();
        boolean isFlip = obj.has("isFlip") && obj.get("isFlip").getAsBoolean();
        String source = fromX + fromY;
        String dest = toX + toY;
        Move move = new Move(source, dest);
        // isFlip 表示本步会翻开棋子；仅 source==destination 时为原地翻子
        move.setFlipOnly(source.equals(dest));
        return move;
    }

    public static JsonObject moveToJson(Move move) {
        JsonObject m = new JsonObject();
        String src = move.getSource();
        String dst = move.getDestination();
        m.addProperty("fromX", String.valueOf(src.charAt(0)));
        m.addProperty("fromY", Character.getNumericValue(src.charAt(1)));
        m.addProperty("toX", String.valueOf(dst.charAt(0)));
        m.addProperty("toY", Character.getNumericValue(dst.charAt(1)));
        m.addProperty("isFlip", move.isFlipOnly());
        return m;
    }

    public static String reasonFromProtocolCode(int reasonCode) {
        return switch (reasonCode) {
            case 0 -> "checkmate";
            case 1 -> "stalemate";
            case 2 -> "timeout";
            case 3 -> "resign";
            case 4 -> "disconnect";
            case 5 -> "king_captured";
            case 6 -> "draw_no_capture";
            case 7 -> "repetition_loss";
            case 8 -> "repetition_draw";
            case 9 -> "draw_agreed";
            default -> "unknown";
        };
    }

    public static int winnerColorFromStatus(com.jieqi.core.Game.GameStatus status) {
        return switch (status) {
            case RED_WIN -> ChessPiece.RED;
            case BLACK_WIN -> ChessPiece.BLACK;
            default -> -1;
        };
    }
}
