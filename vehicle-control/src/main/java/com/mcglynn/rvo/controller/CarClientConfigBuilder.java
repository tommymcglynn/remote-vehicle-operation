package com.mcglynn.rvo.controller;

public final class CarClientConfigBuilder {
    private int commandDelayMin;
    private int commandDelayMax;
    private String videoReceiveHost;
    private int videoReceivePort;

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

    public CarClientConfigBuilder withVideoReceiveHost(String videoReceiveHost) {
        this.videoReceiveHost = videoReceiveHost;
        return this;
    }

    public CarClientConfigBuilder withVideoReceivePort(int videoReceivePort) {
        this.videoReceivePort = videoReceivePort;
        return this;
    }

    public CarClientConfig build() {
        CarClientConfig carClientConfig = new CarClientConfig();
        carClientConfig.setCommandDelayMin(commandDelayMin);
        carClientConfig.setCommandDelayMax(commandDelayMax);
        carClientConfig.setVideoReceiveHost(videoReceiveHost);
        carClientConfig.setVideoReceivePort(videoReceivePort);
        return carClientConfig;
    }
}
