package com.jieqi.network;

import com.jieqi.core.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private int port;
    private ServerSocket serverSocket;
    private Map<String, Game> games;
    private Map<String, ClientHandler> clients;
    private ExecutorService threadPool;
    private boolean running;

    public GameServer(int port) {
        this.port = port;
        this.games = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("揭棋服务器启动，监听端口: " + port);
            threadPool.execute(this::checkTimeout);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            if (running) System.err.println("服务器启动失败: " + e.getMessage());
        }
    }

    private void checkTimeout() {
        while (running) {
            try {
                Thread.sleep(1000);
                for (Game game : games.values()) {
                    if (game.getStatus() == Game.GameStatus.PLAYING && game.isTimeout()) {
                        int timeoutPlayer = game.getCurrentTurn();
                        String winner = (timeoutPlayer == ChessPiece.RED) ? "BLACK_WIN" : "RED_WIN";
                        System.out.println("游戏 " + game.getGameId() + " 超时，玩家 " + timeoutPlayer);
                        broadcastToGame(game.getGameId(), Protocol.buildGameOverMsg(
                            timeoutPlayer == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED));
                    }
                }
            } catch (InterruptedException e) { break; }
        }
    }

    public void broadcastToGame(String gameId, String message) {
        for (ClientHandler client : clients.values()) {
            if (gameId.equals(client.getGameId())) client.sendMessage(message);
        }
    }

    public void sendToPlayer(String gameId, int color, String message) {
        for (ClientHandler client : clients.values()) {
            if (gameId.equals(client.getGameId()) && client.getColor() == color) {
                client.sendMessage(message);
                return;
            }
        }
    }

    public synchronized Game createGame(String gameId) {
        Game game = new Game(gameId);
        games.put(gameId, game);
        return game;
    }

    public Game getGame(String gameId) { return games.get(gameId); }

    public synchronized Game findAvailableGame() {
        for (Game game : games.values()) {
            if (game.getStatus() == Game.GameStatus.WAITING) return game;
        }
        return createGame(UUID.randomUUID().toString().substring(0, 8));
    }

    public void registerClient(String clientId, ClientHandler handler) { clients.put(clientId, handler); }
    public void unregisterClient(String clientId) { clients.remove(clientId); }

    class ClientHandler implements Runnable {
        private Socket socket;
        private GameServer server;
        private PrintWriter out;
        private BufferedReader in;
        private String gameId;
        private int color = -1;
        private String playerName;
        private String clientId;

        ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String loginMsg = in.readLine();
                if (loginMsg == null) return;
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
                if (color == ChessPiece.RED) game.setRedPlayerName(playerName);
                else game.setBlackPlayerName(playerName);

                clientId = gameId + "-" + color;
                server.registerClient(clientId, this);
                System.out.println("玩家 " + playerName + "(" + (color == ChessPiece.RED ? "红" : "黑") + ") 加入游戏 " + gameId);

                sendMessage(Protocol.buildMessage(Protocol.MSG_GAME_STATE, gameId + "|" + color + "|" + game.getStatus().name()));
                sendMessage(Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));

                if (game.getStatus() == Game.GameStatus.PLAYING) {
                    broadcastToGame(gameId, Protocol.buildMessage(Protocol.MSG_GAME_STATE, "START|" + ChessPiece.RED));
                    broadcastToGame(gameId, Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));
                }

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    msgType = Integer.parseInt(inputLine.split("\\|")[0]);
                    data = Protocol.parseData(inputLine);
                    if (msgType == Protocol.MSG_MOVE) {
                        Move move = Protocol.deserializeMove(data);
                        if (move != null) {
                            String error = game.processMove(move, color);
                            if (error != null) sendMessage(Protocol.buildErrorMsg(error));
                            else {
                                String moveMsg = Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(move));
                                broadcastToGame(gameId, moveMsg);
                                broadcastToGame(gameId, Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));
                                Game.GameStatus status = game.getStatus();
                                if (status != Game.GameStatus.PLAYING) {
                                    int winner = (status == Game.GameStatus.RED_WIN) ? ChessPiece.RED :
                                                (status == Game.GameStatus.BLACK_WIN) ? ChessPiece.BLACK : -1;
                                    broadcastToGame(gameId, Protocol.buildGameOverMsg(winner));
                                    System.out.println("游戏 " + gameId + " 结束: " + status);
                                }
                            }
                        }
                    } else if (msgType == Protocol.MSG_QUIT) break;
                }
            } catch (IOException e) {
                System.err.println("客户端断开: " + (playerName != null ? playerName : "unknown"));
            } finally {
                cleanup();
            }
        }

        void sendMessage(String message) { if (out != null) out.println(message); }
        String getGameId() { return gameId; }
        int getColor() { return color; }

        private void cleanup() {
            try {
                if (gameId != null) {
                    Game game = server.getGame(gameId);
                    if (game != null) game.disconnectPlayer(color);
                    server.unregisterClient(clientId);
                }
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8888;
        new GameServer(port).start();
    }
}