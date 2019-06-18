package com.mcglynn.rvo.vehicle.simulation;

import com.mcglynn.rvo.camera.CameraCapture;
import com.mcglynn.rvo.util.Constants;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VideoSendApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoSendApplication.class);
    private static final int DEFAULT_JPEG_QUALITY = 20;
    private static final int CAMERA_FRAMES_PER_SECOND = 15;
    private static final int DEFAULT_CAMERA_ID = 1;

    private CameraCapture cameraCapture;
    private DatagramSocket videoSocket;
    private InetAddress videoTargetAddress;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoSendApplication application = new VideoSendApplication();
        Runtime.getRuntime().addShutdownHook(new Thread(application::shutdown));
        application.start();
    }

    private void start() {
        LOGGER.info("Starting...");

        try {
            videoSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException("Failed to create data socket", e);
        }
        try {
            videoTargetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to create video target address", e);
        }

        cameraCapture = new CameraCapture(DEFAULT_CAMERA_ID, CAMERA_FRAMES_PER_SECOND, this::handleCameraFrame);
        cameraCapture.startCamera();
    }

    private void shutdown() {
        LOGGER.info("Shutting down...");

        if (cameraCapture != null) {
            cameraCapture.stopCamera();
        }
    }

    private void handleCameraFrame(Mat frame) {
        ByteArrayOutputStream payloadOutputStream = new ByteArrayOutputStream();
        byte[] imageBytes = cameraFrameToBytes(frame);
        ByteArrayOutputStream separatorOutputStream = new ByteArrayOutputStream(Constants.IMAGE_STREAM_MARKER.length + 1);
        try {
            separatorOutputStream.write(Constants.IMAGE_STREAM_MARKER);
            separatorOutputStream.write(toByteArray(imageBytes.length));
            payloadOutputStream.write(separatorOutputStream.toByteArray());
            payloadOutputStream.write(imageBytes);
        }
        catch (IOException e) {
            LOGGER.error("Failed to write bytes for output packet", e);
        }

        byte[] payloadBytes = payloadOutputStream.toByteArray();
        int packetSize = 1024;
        int i = 0;
        while (i < payloadBytes.length) {
            int length = Math.min(payloadBytes.length - i, packetSize);
            DatagramPacket packet = new DatagramPacket(payloadBytes, i, length, videoTargetAddress, 9956);
            try {
                videoSocket.send(packet);
            } catch (IOException e) {
                LOGGER.error("Failed to send video packet", e);
                break;
            }
            i += packetSize;
        }


    }

    private byte[] cameraFrameToBytes(Mat frame) {
        MatOfByte jpegBuffer = new MatOfByte();
        MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, DEFAULT_JPEG_QUALITY);
        Imgcodecs.imencode(".jpg", frame, jpegBuffer, params);
        return jpegBuffer.toArray();
    }

    private byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }
}
