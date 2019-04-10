package com.mcglynn.rvo.controller;

public class CarClientConfig {
    private int commandDelayMin;
    private int commandDelayMax;

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

    @Override
    public String toString() {
        return "CarClientConfig{" +
                "commandDelayMin=" + commandDelayMin +
                ", commandDelayMax=" + commandDelayMax +
                '}';
    }
}
