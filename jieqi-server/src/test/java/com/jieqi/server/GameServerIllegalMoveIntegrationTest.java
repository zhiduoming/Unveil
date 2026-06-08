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
 * 非己方回合走子应收到 ERROR 101。
 */
class GameServerIllegalMoveIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void wrongTurnReturnsIllegalMoveError() throws IOException {
        Move blackMove = firstLegalMove(new Board(), ChessPiece.BLACK);
        assertNotNull(blackMove);

        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            connectTwoPlayers(redReader, redSocket, blackReader, blackSocket);

            blackMove.setTurnStartTime(System.currentTimeMillis());
            sendFrame(blackSocket, Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(blackMove)));

            FrameDecoder.DecodedFrame err = awaitError(blackReader, Protocol.ERR_ILLEGAL_MOVE);
            assertNotNull(err);
            assertTrue(err.payload().contains("轮到") || err.payload().contains("回合"));
        }
    }
}
