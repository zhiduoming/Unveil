package com.jieqi.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 基于字节缓冲的协议帧解码器，解决 TCP 粘包/半包。
 * 帧格式：{@code msgType|payloadByteLength|payload}\n（与 {@link Protocol#buildMessage} 一致）
 */
public final class FrameDecoder {

    public static final int MAX_PAYLOAD_BYTES = 64 * 1024;
    public static final int MAX_BUFFER_BYTES = MAX_PAYLOAD_BYTES + 256;

    public record DecodedFrame(int msgType, String payload) {}

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
    private final Queue<DecodedFrame> ready = new ArrayDeque<>();

    public void feed(byte[] chunk, int offset, int length) {
        if (length <= 0) {
            return;
        }
        if (buffer.size() + length > MAX_BUFFER_BYTES) {
            buffer.reset();
            throw new IllegalStateException("协议缓冲超限，连接应关闭");
        }
        buffer.write(chunk, offset, length);
        extractCompleteLines();
    }

    public DecodedFrame poll() {
        return ready.poll();
    }

    public boolean hasBufferedData() {
        return buffer.size() > 0 || !ready.isEmpty();
    }

    private void extractCompleteLines() {
        byte[] raw = buffer.toByteArray();
        int start = 0;
        for (int i = 0; i < raw.length; i++) {
            if (raw[i] != '\n') {
                continue;
            }
            String line = new String(raw, start, i - start, StandardCharsets.UTF_8).trim();
            start = i + 1;
            if (line.isEmpty()) {
                continue;
            }
            DecodedFrame frame = parseLine(line);
            if (frame != null) {
                ready.offer(frame);
            }
        }
        buffer.reset();
        if (start < raw.length) {
            buffer.write(raw, start, raw.length - start);
        }
    }

    private static DecodedFrame parseLine(String line) {
        int first = line.indexOf('|');
        if (first <= 0) {
            return null;
        }
        int second = line.indexOf('|', first + 1);
        if (second <= first) {
            return null;
        }
        try {
            int msgType = Integer.parseInt(line.substring(0, first));
            int declaredLen = Integer.parseInt(line.substring(first + 1, second));
            if (declaredLen < 0 || declaredLen > MAX_PAYLOAD_BYTES) {
                return null;
            }
            String payload = line.substring(second + 1);
            int actualLen = payload.getBytes(StandardCharsets.UTF_8).length;
            if (actualLen != declaredLen) {
                return null;
            }
            return new DecodedFrame(msgType, payload);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
