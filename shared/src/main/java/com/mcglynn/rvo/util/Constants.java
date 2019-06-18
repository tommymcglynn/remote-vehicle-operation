package com.mcglynn.rvo.util;

public class Constants {
    public static final double MAX_THROTTLE = 100;
    private static final double MAX_BRAKE = 100;
    public static final double MAX_STEER = 1000;
    public static final byte[] IMAGE_STREAM_MARKER = new byte[]{-1, -100, 20, 120, 36, 99, 6, 76, -10, -32, 20, 120, 83, 99, 7, 41};
    public static final int IMAGE_STREAM_MARKER_SIZE = IMAGE_STREAM_MARKER.length + 4;
}
