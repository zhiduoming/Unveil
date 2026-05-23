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
 * 未知 msgType 被忽略后，合法走子仍正常处理。
 */
class GameServerUnknownMsgIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void ignoresUnknownTypeAndStillProcessesMove() throws IOException {
        Move redMove = firstLegalMove(new Board(), ChessPiece.RED);
        assertNotNull(redMove);

        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            connectTwoPlayers(redReader, redSocket, blackReader, blackSocket);

            sendFrame(redSocket, Protocol.buildMessage(99, "probe"));

            redMove.setTurnStartTime(System.currentTimeMillis());
            sendFrame(redSocket, Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(redMove)));

            FrameDecoder.DecodedFrame moveFrame = awaitMoveBroadcast(blackReader);
            assertNotNull(moveFrame);
        }
    }
}
