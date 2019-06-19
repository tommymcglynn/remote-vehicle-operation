package com.mcglynn.rvo.util;

import java.io.ByteArrayInputStream;

public interface BufferProcessor<T> {
    T process(ByteArrayInputStream data);
}
