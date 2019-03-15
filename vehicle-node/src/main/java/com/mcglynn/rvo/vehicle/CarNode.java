package com.mcglynn.rvo.vehicle;

import com.mcglynn.rvo.data.CarControlProtos;

public interface CarNode {
    CarControlProtos.CarData getCurrentData();
    void handleCommand(CarControlProtos.CarControllerCommand command);
}
