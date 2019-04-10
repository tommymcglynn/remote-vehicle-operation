package com.mcglynn.rvo.vehicle.simulation;

import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.vehicle.CarNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedCarNode implements CarNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedCarNode.class);

    private CarControlProtos.CarControllerCommand lastCommand;

    public SimulatedCarNode() {
        lastCommand = null;
    }

    @Override
    public CarControlProtos.CarData getCurrentData() {
        return CarControlProtos.CarData.newBuilder()
                .setTime(System.currentTimeMillis())
                .setHappy(true)
                .build();
    }

    @Override
    public void handleCommand(CarControlProtos.CarControllerCommand command) {
        LOGGER.debug(String.format("Received car command: throttle(%s) steer(%s) reverse(%s)", command.getThrottle(), command.getSteer(), command.getReverse()));
        if (lastCommand == null || !lastCommand.equals(command)) {
            lastCommand = command;
            LOGGER.info(String.format("New car command: throttle(%s) steer(%s) reverse(%s)", command.getThrottle(), command.getSteer(), command.getReverse()));
        }
    }
}
