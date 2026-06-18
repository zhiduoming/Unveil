package com.jieqi.core;

import java.util.ArrayList;
import java.util.List;

/** 终局摘要（本地 / WebSocket 共用）。 */
public final class GameSummary {

    private final String roomId;
    private final String redPlayer;
    private final String blackPlayer;
    private final String winner;
    private final String reason;
    private final int totalMoves;
    private final String redCaptured;
    private final String blackCaptured;
    private final boolean longCheck;
    private final boolean longChase;
    private final String recordPath;
    private final String replayPath;

    public GameSummary(String roomId, String redPlayer, String blackPlayer, String winner, String reason,
                       int totalMoves, String redCaptured, String blackCaptured,
                       boolean longCheck, boolean longChase, String recordPath, String replayPath) {
        this.roomId = roomId;
        this.redPlayer = redPlayer;
        this.blackPlayer = blackPlayer;
        this.winner = winner;
        this.reason = reason;
        this.totalMoves = totalMoves;
        this.redCaptured = redCaptured;
        this.blackCaptured = blackCaptured;
        this.longCheck = longCheck;
        this.longChase = longChase;
        this.recordPath = recordPath;
        this.replayPath = replayPath;
    }

    public static GameSummary fromGame(Game game, String recordPath, String replayPath) {
        String winner = switch (game.getStatus()) {
            case RED_WIN -> "红方";
            case BLACK_WIN -> "黑方";
            case DRAW -> "和棋";
            default -> "—";
        };
        int reasonCode = game.getGameOverReason();
        boolean longCheck = reasonCode == EndgameJudge.ProtocolReason.REPETITION_LOSS;
        boolean longChase = reasonCode == EndgameJudge.ProtocolReason.REPETITION_LOSS
                || reasonCode == EndgameJudge.ProtocolReason.REPETITION_DRAW;
        return new GameSummary(
                game.getGameId(),
                nullToDash(game.getRedPlayerName()),
                nullToDash(game.getBlackPlayerName()),
                winner,
                reasonLabel(reasonCode),
                game.getRecord().getLines().size(),
                formatCaptured(game.getCapturedPieces(), ChessPiece.RED),
                formatCaptured(game.getCapturedPieces(), ChessPiece.BLACK),
                longCheck,
                longChase,
                recordPath,
                replayPath
        );
    }

    public void print() {
        System.out.println("===== 对局摘要 =====");
        System.out.println("房间号   " + nullToDash(roomId));
        System.out.println("红方     " + redPlayer);
        System.out.println("黑方     " + blackPlayer);
        System.out.println("胜者     " + winner);
        System.out.println("原因     " + reason);
        System.out.println("总步数   " + totalMoves);
        System.out.println("红方被吃 " + (redCaptured.isEmpty() ? "无" : redCaptured));
        System.out.println("黑方被吃 " + (blackCaptured.isEmpty() ? "无" : blackCaptured));
        System.out.println("触发长将  " + (longCheck ? "是" : "否"));
        System.out.println("触发长捉  " + (longChase ? "是" : "否"));
        if (recordPath != null) {
            System.out.println("棋谱文件 " + recordPath);
        }
        if (replayPath != null) {
            System.out.println("复盘文件 " + replayPath);
        }
        System.out.println("====================");
    }

    private static String formatCaptured(List<ChessPiece> all, int color) {
        List<String> names = new ArrayList<>();
        for (ChessPiece p : all) {
            if (p.getColor() == color) {
                names.add(ChessPiece.getTypeName(p.getType(), p.getColor()));
            }
        }
        return names.stream().collect(java.util.stream.Collectors.joining(" "));
    }

    private static String reasonLabel(int code) {
        return switch (code) {
            case EndgameJudge.ProtocolReason.CHECKMATE -> "将死";
            case EndgameJudge.ProtocolReason.STALEMATE -> "困毙";
            case EndgameJudge.ProtocolReason.TIMEOUT -> "超时";
            case EndgameJudge.ProtocolReason.KING_CAPTURED -> "吃将";
            case EndgameJudge.ProtocolReason.NO_CAPTURE_DRAW -> "40步无吃子和棋";
            case EndgameJudge.ProtocolReason.REPETITION_LOSS -> "长将/长捉判负";
            case EndgameJudge.ProtocolReason.REPETITION_DRAW -> "兵卒长捉和棋";
            default -> "其他";
        };
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }
}
