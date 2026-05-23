package com.jieqi.server;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class GameServerTurnChangeIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void broadcastsTurnChangeAfterLegalMove() throws IOException {
        Move redMove = firstLegalMove(new Board(), ChessPiece.RED);
        assertNotNull(redMove);

        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            connectTwoPlayers(redReader, redSocket, blackReader, blackSocket);

            redMove.setTurnStartTime(System.currentTimeMillis());
            sendFrame(redSocket, Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(redMove)));

            assertNotNull(awaitTurnChange(redReader, ChessPiece.BLACK));
            assertNotNull(awaitTurnChange(blackReader, ChessPiece.BLACK));
        }
    }
}
