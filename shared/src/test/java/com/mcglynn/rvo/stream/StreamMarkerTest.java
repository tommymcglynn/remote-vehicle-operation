package com.mcglynn.rvo.stream;

import com.mcglynn.rvo.util.ByteUtil;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StreamMarkerTest {

    private StreamMarker streamMarker;

    @Before
    public void before() {
        streamMarker = new StreamMarker(new byte[]{1, 2, 10, 20});
    }

    @Test
    public void find() throws IOException {
        byte[] multiMarkerData = ByteUtil.combine(new byte[]{1,2,3},
                streamMarker.wrapData("Hello".getBytes()),
                streamMarker.wrapData("How ya doing".getBytes()),
                streamMarker.wrapData("Bye".getBytes()),
                new byte[]{4,5,6});

        StreamMarkerData streamMarkerData = streamMarker.find(Unpooled.wrappedBuffer(multiMarkerData));
        assertNotNull(streamMarkerData);
        assertEquals(3, streamMarkerData.getIndices().size());

        assertEquals(3, streamMarkerData.getIndices().get(0).getMarkerOffset());
        assertEquals(11, streamMarkerData.getIndices().get(0).getDataOffset());
        assertEquals(5, streamMarkerData.getIndices().get(0).getDataSize());

        assertEquals(16, streamMarkerData.getIndices().get(1).getMarkerOffset());
        assertEquals(24, streamMarkerData.getIndices().get(1).getDataOffset());
        assertEquals(12, streamMarkerData.getIndices().get(1).getDataSize());

        assertEquals(36, streamMarkerData.getIndices().get(2).getMarkerOffset());
        assertEquals(44, streamMarkerData.getIndices().get(2).getDataOffset());
        assertEquals(3, streamMarkerData.getIndices().get(2).getDataSize());
    }
}
