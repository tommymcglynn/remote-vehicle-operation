package com.mcglynn.rvo.controller;

public final class CarControllerConfigBuilder {
    private int commandDelay;

    private CarControllerConfigBuilder() {
    }

    public static CarControllerConfigBuilder aCarControllerConfig() {
        return new CarControllerConfigBuilder();
    }

    public CarControllerConfigBuilder withCommandDelay(int commandDelay) {
        this.commandDelay = commandDelay;
        return this;
    }

    public CarControllerConfig build() {
        CarControllerConfig carControllerConfig = new CarControllerConfig();
        carControllerConfig.setCommandDelay(commandDelay);
        return carControllerConfig;
    }
}
