package com.mcglynn.rvo.camera;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CameraCapture {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraCapture.class);

    private int cameraId = 0;
    private Consumer<Mat> frameConsumer = f -> {};
    private VideoCapture capture = new VideoCapture();
    private ScheduledExecutorService timer;
    private boolean isCameraActive = false;
    private int framesPerSecond;

    public CameraCapture(int cameraId, int framesPerSecond, Consumer<Mat> frameConsumer) {
        this.cameraId = cameraId;
        this.frameConsumer = frameConsumer;
        this.framesPerSecond = Math.min(60, Math.max(1, framesPerSecond));
    }

    private void grabFrame() {
        if (!isCameraActive || !capture.isOpened()) return;
        Mat frame = new Mat();
        capture.read(frame);
        frameConsumer.accept(frame);
    }

    public synchronized boolean startCamera() {
        if (isCameraActive) return true;

        LOGGER.info("Starting camera: cameraId({}) framesPerSecond({})", cameraId, framesPerSecond);

        // start the video capture
        capture.open(cameraId);

        if (capture.isOpened()) {
            isCameraActive = true;

            timer = Executors.newSingleThreadScheduledExecutor();
            double period = 1000d / framesPerSecond;
            timer.scheduleAtFixedRate(this::grabFrame, 0, Math.round(period), TimeUnit.MILLISECONDS);
        }
        else {
            LOGGER.error("Failed to start camera.");
            return false;
        }

        return true;
    }

    public synchronized void stopCamera() {
        if (!isCameraActive) return;

        LOGGER.info("Stopping camera");

        timer.shutdown();

        if (capture.isOpened())
        {
            // release the camera
            capture.release();
        }

        isCameraActive = false;
    }

    public boolean isCapturing() {
        return isCameraActive;
    }
}
