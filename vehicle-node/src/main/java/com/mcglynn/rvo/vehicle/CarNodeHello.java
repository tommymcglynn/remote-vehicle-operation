package com.mcglynn.rvo.vehicle;

import com.mcglynn.rvo.data.CarControlProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarNodeHello implements CarNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarNodeHello.class);

    @Override
    public CarControlProtos.CarData getCurrentData() {
        return CarControlProtos.CarData.newBuilder()
                .setTime(System.currentTimeMillis())
                .setHappy(true)
                .build();
    }

    @Override
    public void handleCommand(CarControlProtos.CarControllerCommand command) {
        LOGGER.info(String.format("Handling command: brake(%d) throttle(%d) steer(%s)", command.getBrake(), command.getThrottle(), command.getSteer()));
    }
}
