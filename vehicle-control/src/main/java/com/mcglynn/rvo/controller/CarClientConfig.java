package com.mcglynn.rvo.controller;

public class CarClientConfig {
    private int commandDelayMin;
    private int commandDelayMax;
    private String videoReceiveHost;
    private int videoReceivePort;

    public int getCommandDelayMin() {
        return commandDelayMin;
    }

    public void setCommandDelayMin(int commandDelayMin) {
        this.commandDelayMin = commandDelayMin;
    }

    public int getCommandDelayMax() {
        return commandDelayMax;
    }

    public void setCommandDelayMax(int commandDelayMax) {
        this.commandDelayMax = commandDelayMax;
    }

    public String getVideoReceiveHost() {
        return videoReceiveHost;
    }

    public void setVideoReceiveHost(String videoReceiveHost) {
        this.videoReceiveHost = videoReceiveHost;
    }

    public int getVideoReceivePort() {
        return videoReceivePort;
    }

    public void setVideoReceivePort(int videoReceivePort) {
        this.videoReceivePort = videoReceivePort;
    }

    @Override
    public String toString() {
        return "CarClientConfig{" +
                "commandDelayMin=" + commandDelayMin +
                ", commandDelayMax=" + commandDelayMax +
                '}';
    }
}
