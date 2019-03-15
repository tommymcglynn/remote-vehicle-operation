package com.mcglynn.rvo.controller;

import com.mcglynn.rvo.data.CarControlProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarControllerHello implements CarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarControllerHello.class);

    @Override
    public CarControlProtos.CarControllerCommand getCurrentCommand() {
        return CarControlProtos.CarControllerCommand.newBuilder()
                .setTime(System.currentTimeMillis())
                .setBrake(0)
                .setThrottle(0)
                .setSteer(0)
                .setReverse(false)
                .build();
    }

    @Override
    public void handleCarData(CarControlProtos.CarData carData) {
        LOGGER.info(String.format("Handle car data: %s", carData));
    }
}
