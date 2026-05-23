package com.jieqi.server;

import com.jieqi.protocol.ProtocolReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 双客户端 TCP 登录：LOGIN_ACK + 双方到齐后开局广播。
 */
class GameServerLoginIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void twoPlayersLoginAndStartGame() throws IOException {
        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            String gameId = loginAndDrain(redReader, redSocket, 0, "Red", "");
            loginAndDrain(blackReader, blackSocket, 1, "Black", gameId);

            assertTrue(awaitGameStart(redReader) || awaitGameStart(blackReader));
        }
    }
}
