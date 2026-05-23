package com.jieqi.server;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractGameServerIntegrationTest {

    protected GameServer server;
    private Thread serverThread;

    @BeforeEach
    void startServer() throws InterruptedException {
        server = new GameServer(0);
        serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
        while (server.getBoundPort() < 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(server.getBoundPort() > 0, "server did not bind in time");
    }

    @AfterEach
    void stopServer() throws InterruptedException {
        if (server != null) {
            server.stop();
        }
        if (serverThread != null) {
            serverThread.join(2000);
        }
    }

    protected static void sendFrame(Socket socket, String frame) throws IOException {
        byte[] bytes = (frame + "\n").getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
    }

    protected static String loginAndDrain(ProtocolReader reader, Socket socket, int color, String name, String gameId)
            throws IOException {
        sendFrame(socket, Protocol.buildLoginMsg(color, name, gameId != null ? gameId : ""));
        String id = awaitLoginAck(reader);
        assertNotNull(id);
        awaitBoardState(reader);
        return id;
    }

    protected static String awaitLoginAck(ProtocolReader reader) throws IOException {
        FrameDecoder.DecodedFrame frame = awaitFrame(reader, Protocol.MSG_GAME_STATE, "LOGIN_ACK|");
        if (frame == null) {
            return null;
        }
        String[] parts = frame.payload().split("\\|", -1);
        return parts.length > 1 ? parts[1] : null;
    }

    protected static boolean awaitBoardState(ProtocolReader reader) throws IOException {
        return awaitFrame(reader, Protocol.MSG_BOARD_STATE, null) != null;
    }

    protected static boolean awaitGameStart(ProtocolReader reader) throws IOException {
        return awaitFrame(reader, Protocol.MSG_GAME_STATE, "GAME_START|") != null;
    }

    protected static FrameDecoder.DecodedFrame awaitMoveBroadcast(ProtocolReader reader) throws IOException {
        return awaitFrame(reader, Protocol.MSG_MOVE, null);
    }

    protected static FrameDecoder.DecodedFrame awaitFrame(
            ProtocolReader reader, int msgType, String payloadPrefix) throws IOException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            FrameDecoder.DecodedFrame frame = reader.readFrame();
            if (frame == null) {
                break;
            }
            if (frame.msgType() == msgType
                    && (payloadPrefix == null || frame.payload().startsWith(payloadPrefix))) {
                return frame;
            }
        }
        return null;
    }

    protected static Move firstLegalMove(Board board, int color) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece piece = board.getPiece(r, c);
                if (piece == null || piece.getColor() != color) {
                    continue;
                }
                String src = ChessPiece.toCoord(r, c);
                for (int dr = 0; dr < 10; dr++) {
                    for (int dc = 0; dc < 9; dc++) {
                        String dst = ChessPiece.toCoord(dr, dc);
                        if (src.equals(dst)) {
                            continue;
                        }
                        Move move = new Move(src, dst);
                        if (RuleValidator.isValidMove(board, move, color)
                                && RuleValidator.isMoveLegal(board, move, color)) {
                            return move;
                        }
                    }
                }
            }
        }
        return null;
    }
}
