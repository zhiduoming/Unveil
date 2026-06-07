package com.jieqi.core;

import com.jieqi.record.GameRecord;

import java.util.*;

public class Game {
    public enum GameStatus { WAITING, PLAYING, RED_WIN, BLACK_WIN, DRAW, TIMEOUT }

    // 游戏结束原因码（对齐 Protocol.reasonCode）
    private int gameOverReason = -1;

    private String gameId;
    private Board board;
    private int currentTurn;
    private GameStatus status;
    private String redPlayerName, blackPlayerName;
    private long turnStartTime;
    private boolean redConnected, blackConnected;
    private final GameRecord record = new GameRecord();
    private Map<String, Integer> repetitionCount;

    public Game(String gameId) {
        this.gameId = gameId;
        this.board = new Board();
        this.currentTurn = ChessPiece.RED;
        this.status = GameStatus.WAITING;
        this.turnStartTime = System.currentTimeMillis();
        this.repetitionCount = new HashMap<>();
    }

    public String processMove(Move move, int playerColor) {
        if (status != GameStatus.PLAYING) return "游戏未在进行中";
        if (playerColor != currentTurn) return "不是你的回合";
        if (isTimeout()) {
            status = (currentTurn == ChessPiece.RED) ? GameStatus.BLACK_WIN : GameStatus.RED_WIN;
            gameOverReason = EndgameJudge.ProtocolReason.TIMEOUT;
            return "超时判负";
        }
        if (move.isFlipOnly() || move.getSource().equals(move.getDestination())) {
            return "禁止原地翻子";
        }
        if (!RuleValidator.isValidMove(board, move, playerColor)) return "非法走法";
        if (!RuleValidator.isMoveLegal(board, move, playerColor)) return "不能送将";

        move.setServerTimestamp(System.currentTimeMillis());
        move.setTurnStartTime(turnStartTime);

        ChessPiece captured = board.executeMove(move);
        record.append(move);

        int nextTurn = (playerColor == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
        String boardHash = getBoardHash(nextTurn);
        EndgameJudge.Verdict verdict = EndgameJudge.checkAfterMove(
                board, playerColor, captured, repetitionCount, boardHash, move);
        if (verdict != null) {
            status = verdict.status();
            gameOverReason = verdict.reasonCode();
            return null;
        }

        int oppColor = (playerColor == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
        currentTurn = oppColor;
        turnStartTime = System.currentTimeMillis();
        return null;
    }

    private String getBoardHash(int sideToMove) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null) sb.append(".");
                else sb.append(p.getColor()).append(p.isRevealed() ? p.getType() : "?");
            }
        }
        sb.append('|').append(sideToMove);
        return sb.toString();
    }

    /** 测试用：调整回合开始时间以触发超时。 */
    public void setTurnStartTime(long turnStartTime) {
        this.turnStartTime = turnStartTime;
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - turnStartTime > 65000;
    }

    public void setStatus(GameStatus status) { this.status = status; }
    public int getGameOverReason() { return gameOverReason; }
    public void setGameOverReason(int reason) { this.gameOverReason = reason; }

    public boolean connectPlayer(int color) {
        if (color == ChessPiece.RED) {
            if (redConnected) return false;
            redConnected = true;
        } else {
            if (blackConnected) return false;
            blackConnected = true;
        }
        if (redConnected && blackConnected && status == GameStatus.WAITING) {
            status = GameStatus.PLAYING;
            turnStartTime = System.currentTimeMillis();
        }
        return true;
    }

    public boolean isRedConnected() { return redConnected; }
    public boolean isBlackConnected() { return blackConnected; }

    public int connectedPlayerCount() {
        return (redConnected ? 1 : 0) + (blackConnected ? 1 : 0);
    }

    public boolean isFinished() {
        return status != GameStatus.WAITING && status != GameStatus.PLAYING;
    }

    public void disconnectPlayer(int color) {
        if (color == ChessPiece.RED) {
            redConnected = false;
            if (status == GameStatus.PLAYING) status = GameStatus.BLACK_WIN;
        } else {
            blackConnected = false;
            if (status == GameStatus.PLAYING) status = GameStatus.RED_WIN;
        }
    }

    // Getters
    public String getGameId() { return gameId; }
    public Board getBoard() { return board; }

    /** 测试/残局：替换棋盘状态。 */
    void replaceBoard(Board board) {
        this.board = board;
    }

    void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public int getCurrentTurn() { return currentTurn; }
    public GameStatus getStatus() { return status; }
    public long getTurnStartTime() { return turnStartTime; }
    public String getRedPlayerName() { return redPlayerName; }
    public String getBlackPlayerName() { return blackPlayerName; }
    public GameRecord getRecord() { return record; }

    /** @deprecated 使用 {@link #getRecord()} */
    @Deprecated
    public List<String> getMoveNotation() { return record.getLines(); }
    public void setRedPlayerName(String name) { this.redPlayerName = name; }
    public void setBlackPlayerName(String name) { this.blackPlayerName = name; }
}
