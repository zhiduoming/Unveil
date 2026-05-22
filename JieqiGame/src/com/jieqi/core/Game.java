package com.jieqi.core;

import java.util.*;

public class Game {
    public enum GameStatus { WAITING, PLAYING, RED_WIN, BLACK_WIN, DRAW, TIMEOUT }

    private String gameId;
    private Board board;
    private int currentTurn;
    private GameStatus status;
    private String redPlayerName, blackPlayerName;
    private long turnStartTime;
    private boolean redConnected, blackConnected;
    private List<String> moveNotation;
    private Map<String, Integer> repetitionCount;

    public Game(String gameId) {
        this.gameId = gameId;
        this.board = new Board();
        this.currentTurn = ChessPiece.RED;
        this.status = GameStatus.WAITING;
        this.turnStartTime = System.currentTimeMillis();
        this.moveNotation = new ArrayList<>();
        this.repetitionCount = new HashMap<>();
    }

    public String processMove(Move move, int playerColor) {
        if (status != GameStatus.PLAYING) return "游戏未在进行中";
        if (playerColor != currentTurn) return "不是你的回合";
        if (isTimeout()) {
            status = (currentTurn == ChessPiece.RED) ? GameStatus.BLACK_WIN : GameStatus.RED_WIN;
            return "超时判负";
        }
        if (!RuleValidator.isValidMove(board, move, playerColor)) return "非法走法";
        if (!RuleValidator.isMoveLegal(board, move, playerColor)) return "走子后将被将军";

        move.setServerTimestamp(System.currentTimeMillis());
        move.setTurnStartTime(turnStartTime);

        ChessPiece captured = board.executeMove(move);
        moveNotation.add(move.toString());

        int oppColor = (playerColor == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;

        if (captured != null && captured.isRevealed() && captured.getType() == ChessPiece.KING) {
            status = (playerColor == ChessPiece.RED) ? GameStatus.RED_WIN : GameStatus.BLACK_WIN;
            return null;
        }
        if (RuleValidator.isCheckmate(board, oppColor) || RuleValidator.isStalemate(board, oppColor)) {
            status = (playerColor == ChessPiece.RED) ? GameStatus.RED_WIN : GameStatus.BLACK_WIN;
            return null;
        }
        if (board.getNoCaptureCount() >= 80) {
            status = GameStatus.DRAW;
            return null;
        }

        String boardHash = getBoardHash();
        repetitionCount.put(boardHash, repetitionCount.getOrDefault(boardHash, 0) + 1);
        if (repetitionCount.get(boardHash) >= 6) {
            status = GameStatus.DRAW;
            return null;
        }

        currentTurn = oppColor;
        turnStartTime = System.currentTimeMillis();
        return null;
    }

    private String getBoardHash() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null) sb.append(".");
                else sb.append(p.getColor()).append(p.isRevealed() ? p.getType() : "?");
            }
        }
        return sb.toString();
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - turnStartTime > 65000;
    }

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
    public int getCurrentTurn() { return currentTurn; }
    public GameStatus getStatus() { return status; }
    public long getTurnStartTime() { return turnStartTime; }
    public String getRedPlayerName() { return redPlayerName; }
    public String getBlackPlayerName() { return blackPlayerName; }
    public List<String> getMoveNotation() { return new ArrayList<>(moveNotation); }
    public void setRedPlayerName(String name) { this.redPlayerName = name; }
    public void setBlackPlayerName(String name) { this.blackPlayerName = name; }
}