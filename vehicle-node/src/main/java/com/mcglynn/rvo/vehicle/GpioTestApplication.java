package com.mcglynn.rvo.vehicle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.GpioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GpioTestApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(GpioTestApplication.class);

    private boolean isRunning = true;
    private boolean isOn = false;

    public static void main(String[] args) {
        GpioTestApplication app = new GpioTestApplication();
        Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));
        try {
            app.run();
        }
        catch (Exception e) {
            LOGGER.error("Failed to run", e);
        }
    }

    private void run() {
        LOGGER.info("Starting!");
        // create gpio controller
        GpioUtil.enableNonPrivilegedAccess();
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput drive1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Drive1", PinState.HIGH);
        final GpioPinDigitalOutput reverse1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Reverse1", PinState.LOW);
        GpioPinPwmOutput speed1 = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_03, "Speed1", 0);
        LOGGER.info("Pins have been provisioned");

        while (isRunning) {
            if (isOn) {
                LOGGER.info("Turning off");
                drive1.setState(PinState.LOW);
                speed1.setPwm(0);
                isOn = false;
            }
            else {
                LOGGER.info("Turning On");
                drive1.setState(PinState.HIGH);
                speed1.setPwm(30);
                isOn = true;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOGGER.error("Error while sleeping", e);
            }
        }
    }

    private void shutdown() {
        LOGGER.info("Shutdown!");
        isRunning = false;
    }
}
