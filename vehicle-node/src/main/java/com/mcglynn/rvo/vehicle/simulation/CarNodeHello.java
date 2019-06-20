package com.mcglynn.rvo.vehicle.simulation;

import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.vehicle.CarNode;
import com.mcglynn.rvo.vehicle.camera.CameraVideoSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarNodeHello implements CarNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarNodeHello.class);

    private CameraVideoSender cameraVideoSender = new CameraVideoSender();

    @Override
    public CarControlProtos.CarData getCurrentData() {
        return CarControlProtos.CarData.newBuilder()
                .setTime(System.currentTimeMillis())
                .setHappy(true)
                .setSendingVideo(cameraVideoSender.isSendingVideo())
                .build();
    }

    @Override
    public void handleCommand(CarControlProtos.CarControllerCommand command) {
        LOGGER.info(String.format("Handling command: brake(%d) throttle(%d) steer(%s)", command.getBrake(), command.getThrottle(), command.getSteer()));
        handleVideoTarget(command.getVideoTargetHost(), command.getVideoTargetPort());
    }

    private void handleVideoTarget(String videoTargetHost, int videoTargetPort) {
        if (videoTargetHost.isEmpty() || videoTargetPort == 0) {
            return;
        }

        cameraVideoSender.setVideoTargetAndStartSendingVideo(videoTargetHost, videoTargetPort);
    }

}
