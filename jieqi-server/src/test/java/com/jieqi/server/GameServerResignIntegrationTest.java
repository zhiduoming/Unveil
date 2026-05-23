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
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认输后广播 GAME_OVER（原因码 RESIGN）并在有着法时落盘棋谱。
 */
class GameServerResignIntegrationTest extends AbstractGameServerIntegrationTest {

    @Test
    void resignEndsGameAndPersistsRecord() throws IOException, InterruptedException {
        Move redMove = firstLegalMove(new Board(), ChessPiece.RED);
        assertNotNull(redMove);

        int port = server.getBoundPort();
        try (Socket redSocket = new Socket("127.0.0.1", port);
             Socket blackSocket = new Socket("127.0.0.1", port)) {

            ProtocolReader redReader = new ProtocolReader(redSocket.getInputStream());
            ProtocolReader blackReader = new ProtocolReader(blackSocket.getInputStream());

            String gameId = connectTwoPlayers(redReader, redSocket, blackReader, blackSocket);

            redMove.setTurnStartTime(System.currentTimeMillis());
            sendFrame(redSocket, Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(redMove)));
            awaitMoveBroadcast(blackReader);

            sendFrame(redSocket, Protocol.buildResignMsg());

            FrameDecoder.DecodedFrame over = awaitGameOver(blackReader);
            assertNotNull(over);
            String[] parts = over.payload().split("\\|", -1);
            assertEquals(3, parts.length);
            assertEquals(String.valueOf(ChessPiece.BLACK), parts[0]);
            assertEquals(String.valueOf(Protocol.REASON_RESIGN), parts[1]);

            Path recordFile = awaitRecordFile(gameId);
            assertNotNull(recordFile);
            assertTrue(Files.readString(recordFile).contains(redMove.getSource()));
        }
    }

    private static Path awaitRecordFile(String gameId) throws InterruptedException, IOException {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            Path found = locateRecordFile(gameId);
            if (found != null) {
                return found;
            }
            Thread.sleep(50);
        }
        return locateRecordFile(gameId);
    }

    private static Path locateRecordFile(String gameId) throws IOException {
        String name = gameId + ".jieqi";
        Path[] candidates = {
                Path.of("records", name),
                Path.of("jieqi-server", "records", name),
        };
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        Path root = Path.of(System.getProperty("user.dir"));
        Path nested = root.resolve("jieqi-server").resolve("records").resolve(name);
        return Files.exists(nested) ? nested : null;
    }
}
