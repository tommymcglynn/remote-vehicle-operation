package com.mcglynn.rvo.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

public class BufferReceiverTest {

    private BufferReceiver<String> bufferReceiver;
    private List<String> output;
    private StreamMarker streamMarker;

    @Before
    public void before() {
        output = new ArrayList<>();
        streamMarker = spy(new StreamMarker(new byte[]{1, 2, 10, 20}));
        bufferReceiver = new BufferReceiver<>(this::stringProcessor, s -> output.add(s), streamMarker, 1000);
    }

    @Test
    public void put() throws IOException {
        bufferReceiver.put(Unpooled.copiedBuffer(streamMarker.wrapData("Hello".getBytes())));
        assertEquals("Hello", output.get(0));

        List<byte[]> dataParts = splitDataInTwo(streamMarker.wrapData("This is some longer data.".getBytes()));
        bufferReceiver.put(Unpooled.wrappedBuffer(dataParts.get(0)));
        assertEquals(1, output.size());
        assertEquals("Hello", output.get(0));
        bufferReceiver.put(Unpooled.wrappedBuffer(dataParts.get(1)));
        assertEquals(2, output.size());
        assertEquals("This is some longer data.", output.get(1));
    }

    @Test
    public void missingData() throws IOException {
        bufferReceiver.put(Unpooled.copiedBuffer(streamMarker.wrapData("Hello".getBytes())));
        assertEquals("Hello", output.get(0));

        List<byte[]> dataParts = splitDataInTwo(streamMarker.wrapData("This is some longer data.".getBytes()));
        bufferReceiver.put(Unpooled.wrappedBuffer(dataWithLastByteRemoved(dataParts.get(0))));
        assertEquals(1, output.size());
        assertEquals("Hello", output.get(0));
        bufferReceiver.put(Unpooled.wrappedBuffer(dataParts.get(1)));
        assertEquals(1, output.size());
        assertEquals("Hello", output.get(0));

        bufferReceiver.put(Unpooled.copiedBuffer(streamMarker.wrapData("Final message".getBytes())));
        assertEquals(2, output.size());
        assertEquals("Final message", output.get(1));
    }

    @Test
    public void multiMarkerPut() throws IOException {
        byte[] multiMarkerData = combine(streamMarker.wrapData("Hello".getBytes()),
                streamMarker.wrapData("How ya doing".getBytes()),
                streamMarker.wrapData("Bye".getBytes()));

        bufferReceiver.put(Unpooled.copiedBuffer(multiMarkerData));
        assertEquals(1, output.size());
        assertEquals("Bye", output.get(0));
    }

    private List<byte[]> splitDataInTwo(byte[] data) {
        ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
        int availableBytes = dataStream.available();
        int partSize1;
        int partSize2;
        if (availableBytes % 2 == 0) {
            partSize1 = partSize2 = availableBytes / 2;
        }
        else {
            partSize1 = availableBytes / 2 + 1;
            partSize2 = partSize1 - 1;
        }
        byte[] dataPart1 = new byte[partSize1];
        dataStream.read(dataPart1, 0, dataPart1.length);
        byte[] dataPart2 = new byte[partSize2];
        dataStream.read(dataPart2, 0, dataPart2.length);

        return Arrays.asList(dataPart1, dataPart2);
    }

    private String stringProcessor(ByteArrayInputStream in) {
        int n = in.available();
        byte[] bytes = new byte[n];
        in.read(bytes, 0, n);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private byte[] dataWithLastByteRemoved(byte[] data) {
        byte[] out = new byte[data.length - 1];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        in.read(out, 0, out.length);
        return out;
    }

    private byte[] combine(byte[]... datas) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] data : datas) {
            try {
                outputStream.write(data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write data", e);
            }
        }

        return outputStream.toByteArray();
    }
}
