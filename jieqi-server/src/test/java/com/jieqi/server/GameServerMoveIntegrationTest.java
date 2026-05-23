package com.jieqi.server;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 双客户端登录后红方合法走子，黑方收到 MOVE 广播。
 */
class GameServerMoveIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void redMoveBroadcastToBlack() throws IOException {
        Move legal = firstLegalMove(new Board(), ChessPiece.RED);
        assertNotNull(legal, "opening position should have a legal move");

        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            String gameId = loginAndDrain(redReader, redSocket, 0, "Red", "");
            loginAndDrain(blackReader, blackSocket, 1, "Black", gameId);
            awaitGameStart(redReader);
            awaitGameStart(blackReader);

            legal.setTurnStartTime(System.currentTimeMillis());
            sendFrame(redSocket, Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(legal)));

            FrameDecoder.DecodedFrame moveFrame = awaitMoveBroadcast(blackReader);
            assertNotNull(moveFrame);
            Move received = Protocol.deserializeMove(moveFrame.payload());
            assertNotNull(received);
            assertEquals(legal.getSource(), received.getSource());
            assertEquals(legal.getDestination(), received.getDestination());
        }
    }
}
