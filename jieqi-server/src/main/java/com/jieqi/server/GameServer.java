package com.jieqi.server;

import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.protocol.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private boolean running;

    public GameServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("揭棋服务器启动，监听端口: " + port);
            System.out.println("默认超时阈值: " + Protocol.TIMEOUT_THRESHOLD + "ms ("
                    + Protocol.STEP_TIME_LIMIT_MS + "ms + " + Protocol.NETWORK_GRACE_MS + "ms 网络裕量)");
            threadPool.execute(this::checkTimeout);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新客户端连接: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("服务器启动失败: " + e.getMessage());
            }
        }
    }

    private void checkTimeout() {
        while (running) {
            try {
                Thread.sleep(1000);
                for (Game game : games.values()) {
                    if (game.getStatus() == Game.GameStatus.PLAYING && game.isTimeout()) {
                        int timeoutPlayer = game.getCurrentTurn();
                        int winner = (timeoutPlayer == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
                        game.setStatus(Game.GameStatus.TIMEOUT);
                        System.out.println("游戏 " + game.getGameId() + " 超时，"
                                + Protocol.getColorName(timeoutPlayer) + " 判负");
                        broadcastToGame(game.getGameId(),
                                Protocol.buildGameOverMsg(winner, Protocol.REASON_TIMEOUT));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void broadcastToGame(String gameId, String message) {
        for (ClientHandler client : clients.values()) {
            if (gameId.equals(client.getGameId())) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * 获取某游戏的所有客户端连接（用于定向转发，如提和只发给对手）。
     */
    public List<ClientHandler> getClientsForGame(String gameId) {
        List<ClientHandler> result = new ArrayList<>();
        for (ClientHandler client : clients.values()) {
            if (gameId.equals(client.getGameId())) {
                result.add(client);
            }
        }
        return result;
    }

    public synchronized Game createGame(String gameId) {
        Game game = new Game(gameId);
        games.put(gameId, game);
        return game;
    }

    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    public synchronized Game findAvailableGame() {
        for (Game game : games.values()) {
            if (game.getStatus() == Game.GameStatus.WAITING) {
                return game;
            }
        }
        return createGame(UUID.randomUUID().toString().substring(0, 8));
    }

    public void registerClient(String clientId, ClientHandler handler) {
        clients.put(clientId, handler);
    }

    public void unregisterClient(String clientId) {
        clients.remove(clientId);
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8888;
        new GameServer(port).start();
    }
}
