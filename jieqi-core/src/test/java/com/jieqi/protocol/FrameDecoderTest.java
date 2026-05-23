package com.jieqi.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FrameDecoderTest {

    @Test
    void decodesSingleFrame() {
        FrameDecoder decoder = new FrameDecoder();
        String line = Protocol.buildMessage(Protocol.MSG_LOGIN, "0|Alice|") + "\n";
        decoder.feed(line.getBytes(StandardCharsets.UTF_8), 0, line.length());
        FrameDecoder.DecodedFrame frame = decoder.poll();
        assertNotNull(frame);
        assertEquals(Protocol.MSG_LOGIN, frame.msgType());
        assertEquals("0|Alice|", frame.payload());
        assertNull(decoder.poll());
    }

    @Test
    void decodesStickyPackets() {
        FrameDecoder decoder = new FrameDecoder();
        String a = Protocol.buildMessage(Protocol.MSG_CHAT, "0|p|hi") + "\n";
        String b = Protocol.buildMessage(Protocol.MSG_QUIT, "") + "\n";
        byte[] bytes = (a + b).getBytes(StandardCharsets.UTF_8);
        decoder.feed(bytes, 0, bytes.length);
        FrameDecoder.DecodedFrame first = decoder.poll();
        FrameDecoder.DecodedFrame second = decoder.poll();
        assertNotNull(first);
        assertEquals(Protocol.MSG_CHAT, first.msgType());
        assertNotNull(second);
        assertEquals(Protocol.MSG_QUIT, second.msgType());
    }

    @Test
    void decodesHalfPacketAcrossFeeds() {
        FrameDecoder decoder = new FrameDecoder();
        String line = Protocol.buildMessage(Protocol.MSG_MOVE, "a0|a1||0|0") + "\n";
        byte[] all = line.getBytes(StandardCharsets.UTF_8);
        int mid = all.length / 2;
        decoder.feed(all, 0, mid);
        assertNull(decoder.poll());
        decoder.feed(all, mid, all.length - mid);
        FrameDecoder.DecodedFrame frame = decoder.poll();
        assertNotNull(frame);
        assertEquals(Protocol.MSG_MOVE, frame.msgType());
    }

    @Test
    void rejectsInvalidPayloadLength() {
        FrameDecoder decoder = new FrameDecoder();
        String bad = "2|999|short\n";
        decoder.feed(bad.getBytes(StandardCharsets.UTF_8), 0, bad.length());
        assertNull(decoder.poll());
    }
}
