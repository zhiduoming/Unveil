package com.jieqi.server;

import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 提和接受后双方收到和棋 GAME_OVER（原因码 AGREED_DRAW）。
 */
class GameServerDrawIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void agreedDrawBroadcastsGameOver() throws IOException {
        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            connectTwoPlayers(redReader, redSocket, blackReader, blackSocket);

            sendFrame(redSocket, Protocol.buildDrawOffer());
            FrameDecoder.DecodedFrame offer = awaitFrame(blackReader, Protocol.MSG_DRAW_REQUEST, "OFFER");
            assertNotNull(offer);

            sendFrame(blackSocket, Protocol.buildDrawResponse(true));

            FrameDecoder.DecodedFrame over = awaitGameOver(redReader);
            if (over == null) {
                over = awaitGameOver(blackReader);
            }
            assertNotNull(over);
            String[] parts = over.payload().split("\\|", -1);
            assertEquals("-1", parts[0]);
            assertEquals(String.valueOf(Protocol.REASON_AGREED_DRAW), parts[1]);
        }
    }
}
