package com.mcglynn.rvo.stream;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class StreamMarker {
    private static final int INTEGER_SIZE = 4;

    private byte[] marker;

    public StreamMarker(byte[] marker) {
        this.marker = marker;
    }

    public int lookForDataSize(ByteBuf byteBuf, int startIndex) {
        for (int i = 0; i < getMarkerLength(); i++) {
            if (byteBuf.getByte(startIndex + i) != getMarker()[i]) {
                return 0;
            }
        }
        byte[] dataSize = new byte[INTEGER_SIZE];
        byteBuf.getBytes(startIndex + getMarkerLength(), dataSize, 0, INTEGER_SIZE);
        return ByteBuffer.wrap(dataSize).getInt();
    }

    public StreamMarkerData find(ByteBuf byteBuf) {
        List<StreamMarkerData.Index> indices = new ArrayList<>();
        for (int i = 0; i < byteBuf.readableBytes(); i++) {
            for (int i2 = 0; i2 < getMarkerLength(); i2++) {
                if (byteBuf.getByte(i + i2) != getMarker()[i2]) {
                    break;
                }
                else if (i2 == getMarkerLength() - 1) {
                    // Found marker
                    byte[] dataSize = new byte[INTEGER_SIZE];
                    byteBuf.getBytes(i + getMarkerLength(), dataSize, 0, INTEGER_SIZE);
                    indices.add(new StreamMarkerData.Index(i, i + getTotalLength(), ByteBuffer.wrap(dataSize).getInt()));
                }
            }

        }
        return new StreamMarkerData(indices);
    }

    public byte[] wrapData(byte[] data) throws IOException {
        ByteArrayOutputStream payloadOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream markerOutputStream = new ByteArrayOutputStream(getTotalLength());
        markerOutputStream.write(getMarker());
        markerOutputStream.write(intToByteArray(data.length));
        payloadOutputStream.write(markerOutputStream.toByteArray());
        payloadOutputStream.write(data);

        return payloadOutputStream.toByteArray();
    }

    private byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    public byte[] getMarker() {
        return marker;
    }

    public int getMarkerLength() {
        return marker.length;
    }

    public int getTotalLength() {
        return getMarkerLength() + INTEGER_SIZE;
    }

}
