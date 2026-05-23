package com.jieqi.server;

import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 双客户端 TCP 登录：LOGIN_ACK + 双方到齐后开局广播。
 */
class GameServerLoginIntegrationTest {

    private GameServer server;
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

    @Test
    void twoPlayersLoginAndStartGame() throws IOException {
        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            sendFrame(redSocket, Protocol.buildLoginMsg(0, "Red", ""));
            String gameId = awaitLoginAck(redReader);
            assertNotNull(gameId);
            assertTrue(awaitBoardState(redReader));

            sendFrame(blackSocket, Protocol.buildLoginMsg(1, "Black", gameId));
            awaitLoginAck(blackReader);
            assertTrue(awaitBoardState(blackReader));

            assertTrue(awaitGameStart(redReader) || awaitGameStart(blackReader));
        }
    }

    private static void sendFrame(Socket socket, String frame) throws IOException {
        byte[] bytes = (frame + "\n").getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
    }

    private static String awaitLoginAck(ProtocolReader reader) throws IOException {
        FrameDecoder.DecodedFrame frame = awaitFrame(reader, Protocol.MSG_GAME_STATE, "LOGIN_ACK|");
        assertNotNull(frame);
        String[] parts = frame.payload().split("\\|", -1);
        return parts.length > 1 ? parts[1] : null;
    }

    private static boolean awaitBoardState(ProtocolReader reader) throws IOException {
        return awaitFrame(reader, Protocol.MSG_BOARD_STATE, null) != null;
    }

    private static boolean awaitGameStart(ProtocolReader reader) throws IOException {
        return awaitFrame(reader, Protocol.MSG_GAME_STATE, "GAME_START|") != null;
    }

    private static FrameDecoder.DecodedFrame awaitFrame(
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
}
