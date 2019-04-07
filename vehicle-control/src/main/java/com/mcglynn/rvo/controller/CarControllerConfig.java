package com.mcglynn.rvo.controller;

public class CarControllerConfig {
    private int commandDelay;

    public int getCommandDelay() {
        return commandDelay;
    }

    public void setCommandDelay(int commandDelay) {
        this.commandDelay = commandDelay;
    }

    @Override
    public String toString() {
        return "CarControllerConfig{" +
                "commandDelay=" + commandDelay +
                '}';
    }
}
