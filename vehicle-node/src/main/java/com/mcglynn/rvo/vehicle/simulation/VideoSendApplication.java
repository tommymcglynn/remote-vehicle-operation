package com.mcglynn.rvo.vehicle.simulation;

import com.mcglynn.rvo.vehicle.camera.CameraVideoSender;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoSendApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoSendApplication.class);
    private static final int DEFAULT_VIDEO_SEND_PORT = 8090;

    private CameraVideoSender cameraVideoSender = new CameraVideoSender();

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoSendApplication application = new VideoSendApplication();
        Runtime.getRuntime().addShutdownHook(new Thread(application::shutdown));
        application.start();
    }

    private void start() {
        LOGGER.info("Starting...");
        cameraVideoSender.setVideoTargetAndStartSendingVideo("localhost", DEFAULT_VIDEO_SEND_PORT);
    }

    private void shutdown() {
        LOGGER.info("Shutting down...");
        cameraVideoSender.stopSendingVideo();
    }

}
