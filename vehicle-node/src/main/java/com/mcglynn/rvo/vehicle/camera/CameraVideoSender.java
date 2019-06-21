package com.mcglynn.rvo.vehicle.camera;

import com.mcglynn.rvo.camera.CameraCapture;
import com.mcglynn.rvo.util.Constants;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CameraVideoSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(CameraVideoSender.class);
    private static final int DEFAULT_JPEG_QUALITY = 20;
    private static final int CAMERA_FRAMES_PER_SECOND = 15;
    private static final int DEFAULT_CAMERA_ID = 1;

    private int cameraId;
    private String videoTargetHost = null;
    private int videoTargetPort = 0;
    private CameraCapture cameraCapture;
    private DatagramSocket videoSocket;
    private InetAddress videoTargetAddress;

    public CameraVideoSender() {
        this.cameraId = DEFAULT_CAMERA_ID;
    }

    public CameraVideoSender(int cameraId) {
        this.cameraId = cameraId;
    }

    public void setVideoTargetAndStartSendingVideo(String host, int port) {
        if (cameraCapture != null) {
            if (host.equals(videoTargetHost) && port == videoTargetPort) {
                return;
            }
            cameraCapture.close();
        }

        if (videoSocket == null) {
            videoSocket = createVideoSocket();
        }

        try {
            videoTargetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to create video target address", e);
        }
        videoTargetPort = port;

        cameraCapture = new CameraCapture(cameraId, CAMERA_FRAMES_PER_SECOND, this::handleCameraFrame);
        cameraCapture.startCamera();
    }

    public void stopSendingVideo() {
        cameraCapture.close();
        cameraCapture = null;
    }

    public boolean isSendingVideo() {
        return cameraCapture != null && cameraCapture.isCameraActive();
    }

    private void handleCameraFrame(Mat frame) {
        byte[] imageBytes = cameraFrameToBytes(frame);
        byte[] payloadBytes;
        try {
            payloadBytes = Constants.DEFAULT_IMAGE_STREAM_MARKER.wrapData(imageBytes);
        } catch (IOException e) {
            LOGGER.error("Failed to write bytes for output packet", e);
            return;
        }
        int packetSize = 1024;
        int i = 0;
        while (i < payloadBytes.length) {
            int length = Math.min(payloadBytes.length - i, packetSize);
            DatagramPacket packet = new DatagramPacket(payloadBytes, i, length, videoTargetAddress, videoTargetPort);
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

    private DatagramSocket createVideoSocket() {
        try {
            return new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException("Failed to create data socket", e);
        }
    }
}
