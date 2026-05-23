package com.jieqi.server;

import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class GameServerChatIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void broadcastsChatAndRateLimitsSender() throws IOException {
        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            connectTwoPlayers(redReader, redSocket, blackReader, blackSocket);

            sendFrame(redSocket, Protocol.buildChatMsg(0, "Red", "hello"));
            FrameDecoder.DecodedFrame chat = awaitChat(blackReader);
            assertNotNull(chat);
            assertTrue(chat.payload().contains("hello"));

            sendFrame(redSocket, Protocol.buildChatMsg(0, "Red", "spam"));
            FrameDecoder.DecodedFrame err = awaitError(redReader, Protocol.ERR_UNKNOWN);
            assertNotNull(err);
            assertTrue(err.payload().contains("频繁"));
        }
    }
}
