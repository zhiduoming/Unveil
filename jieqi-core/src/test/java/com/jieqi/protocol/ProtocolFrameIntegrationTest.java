package com.jieqi.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 协议帧编解码联调：构建 → 字节流 → FrameDecoder 解析。
 */
class ProtocolFrameIntegrationTest {

    @Test
    void loginFrameRoundTrip() {
        String frame = Protocol.buildLoginMsg(0, "Alice", "room1") + "\n";
        FrameDecoder decoder = new FrameDecoder();
        decoder.feed(frame.getBytes(StandardCharsets.UTF_8), 0, frame.length());
        FrameDecoder.DecodedFrame decoded = decoder.poll();
        assertNotNull(decoded);
        assertEquals(Protocol.MSG_LOGIN, decoded.msgType());
        assertEquals("0|Alice|room1", decoded.payload());
    }

    @Test
    void boardStateFramePreservesPayloadLength() {
        String inner = "0|row0|row1";
        String frame = Protocol.buildMessage(Protocol.MSG_BOARD_STATE, inner) + "\n";
        FrameDecoder decoder = new FrameDecoder();
        decoder.feed(frame.getBytes(StandardCharsets.UTF_8), 0, frame.length());
        FrameDecoder.DecodedFrame decoded = decoder.poll();
        assertNotNull(decoded);
        assertEquals(Protocol.MSG_BOARD_STATE, decoded.msgType());
        assertEquals(inner, decoded.payload());
    }
}
