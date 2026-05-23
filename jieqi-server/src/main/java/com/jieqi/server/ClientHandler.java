package com.jieqi.server;

import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;
import com.jieqi.protocol.Protocol;

import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.ProtocolReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private PrintWriter out;
    private ProtocolReader reader;
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
            reader = new ProtocolReader(socket.getInputStream());

            FrameDecoder.DecodedFrame loginFrame = reader.readFrame();
            if (loginFrame == null) {
                return;
            }
            int msgType = loginFrame.msgType();
            if (msgType != Protocol.MSG_LOGIN) {
                sendMessage(Protocol.buildErrorMsg(Protocol.ERR_UNKNOWN, "请先发送 LOGIN 消息"));
                return;
            }
            String data = loginFrame.payload();
            if (data == null) {
                sendMessage(Protocol.buildErrorMsg(Protocol.ERR_MALFORMED_MSG, "LOGIN 消息帧损坏"));
                return;
            }
            String[] loginData = data.split("\\|");
            color = Integer.parseInt(loginData[0]);
            playerName = loginData.length > 1 ? loginData[1] : "Player";
            String requestedGameId = loginData.length > 2 ? loginData[2] : "";

            // 查找或创建游戏
            Game game;
            if (!requestedGameId.isEmpty()) {
                game = server.getGame(requestedGameId);
                if (game == null) {
                    sendMessage(Protocol.buildErrorMsg(Protocol.ERR_ROOM_NOT_FOUND, "指定游戏不存在"));
                    return;
                }
            } else {
                game = server.findAvailableGame();
            }
            gameId = game.getGameId();

            boolean connected = game.connectPlayer(color);
            if (!connected) {
                if (color == ChessPiece.RED) {
                    sendMessage(Protocol.buildErrorMsg(Protocol.ERR_COLOR_TAKEN, "红方已被占用"));
                } else {
                    sendMessage(Protocol.buildErrorMsg(Protocol.ERR_COLOR_TAKEN, "黑方已被占用"));
                }
                return;
            }
            if (color == ChessPiece.RED) {
                game.setRedPlayerName(playerName);
            } else {
                game.setBlackPlayerName(playerName);
            }

            clientId = gameId + "-" + color;
            server.registerClient(clientId, this);
            System.out.println("玩家 " + playerName + "(" + Protocol.getColorName(color) + ") 加入游戏 " + gameId);

            // 回复 LOGIN_ACK
            sendMessage(Protocol.buildLoginAck(gameId, color, game.getStatus().name()));
            // 发送初始棋盘
            sendMessage(Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));

            // 若双方到齐，广播开局
            if (game.getStatus() == Game.GameStatus.PLAYING) {
                server.broadcastToGame(gameId, Protocol.buildGameStart(ChessPiece.RED));
                server.broadcastToGame(gameId, Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));
                server.broadcastToGame(gameId, Protocol.buildTurnChange(game.getCurrentTurn()));
            }

            FrameDecoder.DecodedFrame frame;
            while ((frame = reader.readFrame()) != null) {
                msgType = frame.msgType();
                data = frame.payload();
                switch (msgType) {
                    case Protocol.MSG_MOVE:
                        handleMove(game, data);
                        break;
                    case Protocol.MSG_DRAW_REQUEST:
                        handleDrawRequest(game, data);
                        break;
                    case Protocol.MSG_RESIGN:
                        handleResign(game);
                        break;
                    case Protocol.MSG_CHAT:
                        server.broadcastToGame(gameId,
                                Protocol.buildMessage(Protocol.MSG_CHAT, data));
                        break;
                    case Protocol.MSG_QUIT:
                        handleQuit(game);
                        return;
                    default:
                        // 忽略未知消息类型，不崩溃
                        System.out.println("未知消息类型: " + msgType);
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
            sendMessage(Protocol.buildErrorMsg(Protocol.ERR_MALFORMED_MSG, "MOVE 消息格式错误"));
            return;
        }
        String error = game.processMove(move, color);
        if (error != null) {
            sendMessage(Protocol.buildErrorMsg(Protocol.ERR_ILLEGAL_MOVE, error));
            return;
        }
        // 广播确认后的走法
        String moveMsg = Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(move));
        server.broadcastToGame(gameId, moveMsg);
        // 同步棋盘
        server.broadcastToGame(gameId, Protocol.buildBoardState(game.getBoard(), game.getCurrentTurn()));

        Game.GameStatus status = game.getStatus();
        if (status != Game.GameStatus.PLAYING) {
            broadcastGameOver(game, status);
        } else {
            server.broadcastToGame(gameId, Protocol.buildTurnChange(game.getCurrentTurn()));
        }
    }

    private void handleDrawRequest(Game game, String data) {
        if (data.equals("OFFER")) {
            // 转发提和给对手
            for (ClientHandler client : server.getClientsForGame(gameId)) {
                if (client != this) {
                    client.sendMessage(Protocol.buildDrawOffer());
                }
            }
        } else if (data.equals("ACCEPT")) {
            // 对方同意和棋
            game.setStatus(Game.GameStatus.DRAW);
            game.setGameOverReason(Protocol.REASON_AGREED_DRAW);
            broadcastGameOver(game, Game.GameStatus.DRAW);
        } else if (data.equals("DECLINE")) {
            // 转发拒绝给提和方
            for (ClientHandler client : server.getClientsForGame(gameId)) {
                if (client != this) {
                    client.sendMessage(Protocol.buildDrawResponse(false));
                }
            }
        }
    }

    private void handleResign(Game game) {
        Game.GameStatus result = (color == ChessPiece.RED)
                ? Game.GameStatus.BLACK_WIN : Game.GameStatus.RED_WIN;
        game.setStatus(result);
        game.setGameOverReason(Protocol.REASON_RESIGN);
        server.broadcastToGame(gameId, Protocol.buildResignNotify(color));
        broadcastGameOver(game, result);
    }

    private void handleQuit(Game game) {
        if (game.getStatus() == Game.GameStatus.PLAYING) {
            int winner = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
            game.setStatus(winner == ChessPiece.RED ? Game.GameStatus.RED_WIN : Game.GameStatus.BLACK_WIN);
            server.broadcastToGame(gameId,
                    Protocol.buildGameOverMsg(winner, Protocol.REASON_DISCONNECT));
        }
    }

    private void broadcastGameOver(Game game, Game.GameStatus status) {
        int winner;
        int reasonCode = game.getGameOverReason(); // 优先使用 Game 中已设置的原因码
        switch (status) {
            case RED_WIN:
                winner = ChessPiece.RED;
                if (reasonCode < 0) reasonCode = Protocol.REASON_CHECKMATE;
                break;
            case BLACK_WIN:
                winner = ChessPiece.BLACK;
                if (reasonCode < 0) reasonCode = Protocol.REASON_CHECKMATE;
                break;
            case DRAW:
                winner = -1;
                if (reasonCode < 0) reasonCode = Protocol.REASON_AGREED_DRAW;
                break;
            case TIMEOUT:
                winner = game.getCurrentTurn() == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED;
                reasonCode = Protocol.REASON_TIMEOUT;
                break;
            default:
                return;
        }
        server.broadcastToGame(gameId, Protocol.buildGameOverMsg(winner, reasonCode));
        System.out.println("游戏 " + gameId + " 结束: " + status
                + " (" + Protocol.getReasonDescription(reasonCode) + ")");
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getGameId() { return gameId; }
    public int getColor() { return color; }

    private void cleanup() {
        try {
            if (gameId != null) {
                Game game = server.getGame(gameId);
                if (game != null) {
                    game.disconnectPlayer(color);
                }
                server.unregisterClient(clientId);
            }
            if (reader != null) reader.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
