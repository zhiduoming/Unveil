package com.jieqi.protocol;

import com.jieqi.core.Move;

public class Protocol {
    public static final int MSG_LOGIN = 1;
    public static final int MSG_MOVE = 2;
    public static final int MSG_GAME_STATE = 3;
    public static final int MSG_ERROR = 4;
    public static final int MSG_QUIT = 5;
    public static final int MSG_GAME_OVER = 6;
    public static final int MSG_BOARD_STATE = 7;

    public static String serializeMove(Move move) {
        return move.getSource() + "|" + move.getDestination() + "|" +
               (move.getType() != null ? move.getType() : "") + "|" +
               move.getTurnStartTime() + "|" + (move.isFlipOnly() ? "1" : "0");
    }

    public static Move deserializeMove(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 3) return null;
        Move move = new Move(parts[0], parts[1]);
        if (!parts[2].isEmpty()) move.setType(Integer.parseInt(parts[2]));
        if (parts.length >= 4) move.setTurnStartTime(Long.parseLong(parts[3]));
        if (parts.length >= 5) move.setFlipOnly(parts[4].equals("1"));
        return move;
    }

    public static String buildMessage(int msgType, String data) {
        return msgType + "|" + data.length() + "|" + data;
    }

    public static String parseData(String msg) {
        String[] parts = msg.split("\\|", 3);
        return parts.length == 3 ? parts[2] : "";
    }

    public static String buildLoginMsg(int color, String playerName) {
        return buildMessage(MSG_LOGIN, color + "|" + playerName);
    }

    public static String buildErrorMsg(String error) {
        return buildMessage(MSG_ERROR, error);
    }

    public static String buildGameOverMsg(int winner) {
        return buildMessage(MSG_GAME_OVER, String.valueOf(winner));
    }

    public static String buildBoardState(com.jieqi.core.Board board, int currentTurn) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentTurn).append("|");
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                com.jieqi.core.ChessPiece p = board.getPiece(r, c);
                if (p == null) sb.append(".");
                else sb.append(p.getColor()).append(p.isRevealed() ? p.getType() : "?");
                if (c < 8) sb.append(",");
            }
            sb.append(";");
        }
        return buildMessage(MSG_BOARD_STATE, sb.toString());
    }
}