package com.mcglynn.rvo.image;

import com.mcglynn.rvo.util.Constants;
import io.netty.buffer.ByteBuf;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ImageBufferReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageBufferReceiver.class);
    private static final int DEFAULT_BUFFER_SIZE = 500000;

    private byte[] buffer;
    private int bufPos = 0;
    private Lock bufferLock;
    private int expectedImageDataSize = 0;
    private Consumer<Image> imageHandler;

    public ImageBufferReceiver(Consumer<Image> imageHandler) {
        this.imageHandler = imageHandler;
        buffer = new byte[DEFAULT_BUFFER_SIZE];
        bufferLock = new ReentrantLock();
    }

    public void put(ByteBuf byteBuf) {
        bufferLock.lock();
        try {
            if (writeBytes(byteBuf)) {
                tryToProcessImage();
            }
        }
        finally {
            bufferLock.unlock();
        }
    }

    private void tryToProcessImage() {
        if (expectedImageDataSize != 0 && bufPos >= expectedImageDataSize) {
            Image image = imageFromBytes(buffer);
            bufPos = 0;
            expectedImageDataSize = 0;
            imageHandler.accept(image);
        }
    }

    private boolean writeBytes(ByteBuf byteBuf) {
        int readableBytes = byteBuf.readableBytes();
        if (bufPos == 0) {
            expectedImageDataSize = lookForImageDataSize(byteBuf, bufPos);
            if (expectedImageDataSize != 0) {
                byteBuf.readerIndex(bufPos + Constants.IMAGE_STREAM_MARKER_SIZE);
                readableBytes -= Constants.IMAGE_STREAM_MARKER_SIZE;
            }
        }
        if (bufPos < DEFAULT_BUFFER_SIZE - readableBytes) {
            byteBuf.readBytes(buffer, bufPos, readableBytes);
            bufPos += readableBytes;
        }
        else {
            LOGGER.warn("Buffer full");
            return false;
        }

        return true;
    }

    Image imageFromBytes(byte[] bytes) {
        return new Image(new ByteArrayInputStream(bytes));
    }

    private int lookForImageDataSize(ByteBuf byteBuf, int startIndex) {
        for (int i = 0; i < Constants.IMAGE_STREAM_MARKER.length; i++) {
            if (byteBuf.getByte(startIndex + i) != Constants.IMAGE_STREAM_MARKER[i]) {
                return 0;
            }
        }
        byte[] dataSize = new byte[4];
        byteBuf.getBytes(startIndex + Constants.IMAGE_STREAM_MARKER.length, dataSize, 0, 4);
        return ByteBuffer.wrap(dataSize).getInt();
    }
}
