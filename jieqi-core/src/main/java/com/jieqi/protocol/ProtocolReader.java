package com.jieqi.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 从 {@link InputStream} 按帧读取协议消息（内部使用 {@link FrameDecoder}）。
 */
public final class ProtocolReader implements Closeable {

    private final InputStream in;
    private final FrameDecoder decoder = new FrameDecoder();
    private final byte[] scratch = new byte[8192];

    public ProtocolReader(InputStream in) {
        this.in = in;
    }

    /**
     * 阻塞读取下一完整帧；流结束返回 {@code null}。
     */
    public FrameDecoder.DecodedFrame readFrame() throws IOException {
        while (true) {
            FrameDecoder.DecodedFrame frame = decoder.poll();
            if (frame != null) {
                return frame;
            }
            int n = in.read(scratch);
            if (n < 0) {
                return decoder.poll();
            }
            decoder.feed(scratch, 0, n);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
