package com.jieqi.server;

import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;
import com.jieqi.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String gameId;
    private int color = -1;
    private String playerName;
    private String clientId;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String loginMsg = in.readLine();
            if (loginMsg == null) {
                return;
            }
            int msgType = Integer.parseInt(loginMsg.split("\\|")[0]);
            if (msgType != Protocol.MSG_LOGIN) {
                sendMessage(Protocol.buildErrorMsg("请先登录"));
                return;
            }
            String data = Protocol.parseData(loginMsg);
            String[] loginData = data.split("\\|");
            color = Integer.parseInt(loginData[0]);
            playerName = loginData.length > 1 ? loginData[1] : "Player";

            Game game = server.findAvailableGame();
            gameId = game.getGameId();
            game.connectPlayer(color);
            if (color == ChessPiece.RED) {
                game.setRedPlayerName(playerName);
            } else {
                game.setBlackPlayerName(playerName);
            }

            clientId = gameId + "-" + color;
            server.registerClient(clientId, this);
            System.out.println("玩家 " + playerName + "(" + (color == ChessPiece.RED ? "红" : "黑") + ") 加入游戏 " + gameId);

            sendMessage(Protocol.buildMessage(Protocol.MSG_GAME_STATE,
                    gameId + "|" + color + "|" + game.getStatus().name()));
            sendMessage(Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));

            if (game.getStatus() == Game.GameStatus.PLAYING) {
                server.broadcastToGame(gameId, Protocol.buildMessage(Protocol.MSG_GAME_STATE, "START|" + ChessPiece.RED));
                server.broadcastToGame(gameId, Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                msgType = Integer.parseInt(inputLine.split("\\|")[0]);
                data = Protocol.parseData(inputLine);
                if (msgType == Protocol.MSG_MOVE) {
                    handleMove(game, data);
                } else if (msgType == Protocol.MSG_QUIT) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("客户端断开: " + (playerName != null ? playerName : "unknown"));
        } finally {
            cleanup();
        }
    }

    private void handleMove(Game game, String data) {
        Move move = Protocol.deserializeMove(data);
        if (move == null) {
            return;
        }
        String error = game.processMove(move, color);
        if (error != null) {
            sendMessage(Protocol.buildErrorMsg(error));
            return;
        }
        String moveMsg = Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(move));
        server.broadcastToGame(gameId, moveMsg);
        server.broadcastToGame(gameId, Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));
        Game.GameStatus status = game.getStatus();
        if (status != Game.GameStatus.PLAYING) {
            int winner = (status == Game.GameStatus.RED_WIN) ? ChessPiece.RED
                    : (status == Game.GameStatus.BLACK_WIN) ? ChessPiece.BLACK : -1;
            server.broadcastToGame(gameId, Protocol.buildGameOverMsg(winner));
            System.out.println("游戏 " + gameId + " 结束: " + status);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getGameId() {
        return gameId;
    }

    public int getColor() {
        return color;
    }

    private void cleanup() {
        try {
            if (gameId != null) {
                Game game = server.getGame(gameId);
                if (game != null) {
                    game.disconnectPlayer(color);
                }
                server.unregisterClient(clientId);
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
