package com.mcglynn.rvo.stream;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class BufferReceiver<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferReceiver.class);

    private byte[] buffer;
    private int bufPos = 0;
    private Lock bufferLock;
    private int expectedDataSize = 0;
    private BufferProcessor<T> processor;
    private Consumer<T> handler;
    private StreamMarker streamMarker;

    public BufferReceiver(BufferProcessor<T> processor, Consumer<T> handler, StreamMarker streamMarker, int bufferSize) {
        this.processor = processor;
        this.handler = handler;
        this.streamMarker = streamMarker;
        buffer = new byte[bufferSize];
        bufferLock = new ReentrantLock();
    }

    public void put(ByteBuf byteBuf) {
        bufferLock.lock();
        try {
            if (writeBytes(byteBuf)) {
                tryToProcessData();
            }
        }
        finally {
            bufferLock.unlock();
        }
    }

    private void tryToProcessData() {
        if (expectedDataSize != 0 && bufPos >= expectedDataSize) {
            T output = processor.process(new ByteArrayInputStream(buffer, 0, expectedDataSize));
            bufPos = 0;
            expectedDataSize = 0;
            handler.accept(output);
        }
    }

    private boolean writeBytes(ByteBuf byteBuf) {
        int readableBytes = byteBuf.readableBytes();
        int newExpectedSize = streamMarker.lookForDataSize(byteBuf, 0);
        if (newExpectedSize != 0) {
            if (expectedDataSize != 0) {
                bufPos = 0;
            }
            expectedDataSize = newExpectedSize;
            byteBuf.readerIndex(bufPos + streamMarker.getTotalLength());
            readableBytes -= streamMarker.getTotalLength();
        }
        if (bufPos < buffer.length - readableBytes) {
            byteBuf.readBytes(buffer, bufPos, readableBytes);
            bufPos += readableBytes;
        }
        else {
            LOGGER.warn("Buffer full");
            return false;
        }

        return true;
    }

}
