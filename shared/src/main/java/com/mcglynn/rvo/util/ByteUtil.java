package com.mcglynn.rvo.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ByteUtil {
    public static byte[] combine(byte[]... datas) {
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
