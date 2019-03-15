package com.mcglynn.rvo.controller;

import com.mcglynn.rvo.data.CarControlProtos;

public interface CarController {
    CarControlProtos.CarControllerCommand getCurrentCommand();
    void handleCarData(CarControlProtos.CarData carData);
}
