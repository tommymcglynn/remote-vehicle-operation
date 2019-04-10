package com.mcglynn.rvo.vehicle.toy;

import org.pushingpixels.trident.Timeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnimationTestApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnimationTestApplication.class);

    private double amount = 0d;

    public static void main(String[] args) {
        try {
            new AnimationTestApplication().run();
        }
        catch (Exception e) {
            LOGGER.error("Failure while running", e);
        }
    }

    private void run() throws InterruptedException {
        Timeline timeline = new Timeline(this);
        timeline.addPropertyToInterpolate("amount", amount, 100d);
        timeline.play();

        Thread.sleep(3000);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        LOGGER.info(String.format("amount=%s", amount));
    }
}
