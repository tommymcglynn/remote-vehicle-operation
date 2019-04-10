package com.mcglynn.rvo.controller;

public final class CarClientConfigBuilder {
    private int commandDelayMin;
    private int commandDelayMax;

    private CarClientConfigBuilder() {
    }

    public static CarClientConfigBuilder aCarClientConfig() {
        return new CarClientConfigBuilder();
    }

    public CarClientConfigBuilder withCommandDelayMin(int commandDelayMin) {
        this.commandDelayMin = commandDelayMin;
        return this;
    }

    public CarClientConfigBuilder withCommandDelayMax(int commandDelayMax) {
        this.commandDelayMax = commandDelayMax;
        return this;
    }

    public CarClientConfig build() {
        CarClientConfig carClientConfig = new CarClientConfig();
        carClientConfig.setCommandDelayMin(commandDelayMin);
        carClientConfig.setCommandDelayMax(commandDelayMax);
        return carClientConfig;
    }
}
