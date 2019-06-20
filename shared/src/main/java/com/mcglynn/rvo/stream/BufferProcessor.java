package com.mcglynn.rvo.stream;

import java.io.ByteArrayInputStream;

public interface BufferProcessor<T> {
    T process(ByteArrayInputStream data);
}
