package com.mcglynn.rvo.vehicle.toy;

import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.util.Constants;
import com.mcglynn.rvo.vehicle.CarNode;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.GpioUtil;
import org.pushingpixels.trident.Timeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FourWheelToyCarNode implements CarNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(FourWheelToyCarNode.class);
    private static final double THROTTLE_MULTIPLIER = 1d;
    private static final double SPIN_THROTTLE_REDUCTION = (double) Constants.MAX_THROTTLE / (double) Constants.MAX_STEER;
    private static final int PWM_RANGE = 20;
    private static final double PWM_RANGE_DRIVE_REDUCTION = (double) PWM_RANGE / 100d;

    private static boolean pinsProvisioned = false;
    private static GpioPinDigitalOutput leftDrivePin;
    private static GpioPinDigitalOutput leftReversePin;
    private static GpioPinPwmOutput leftSpeedPin;
    private static GpioPinDigitalOutput rightDrivePin;
    private static GpioPinDigitalOutput rightReversePin;
    private static GpioPinPwmOutput rightSpeedPin;

    private Integer leftDrive = 0;
    private Integer rightDrive = 0;
    private Integer leftDriveTarget = 0;
    private Integer rightDriveTarget = 0;
    private Timeline leftDriveTimeline = null;
    private Timeline rightDriveTimeline = null;

    public FourWheelToyCarNode() {
        provisionGpioPins();
    }

    private synchronized void provisionGpioPins() {
        if (pinsProvisioned) return;
        LOGGER.info("Will try to provision GPIO pins");
        GpioUtil.enableNonPrivilegedAccess();
        final GpioController gpio = GpioFactory.getInstance();
        leftDrivePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "leftDrive", PinState.HIGH);
        leftReversePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "leftReverse", PinState.LOW);
        leftSpeedPin = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_25, "leftSpeed", 0);
        leftSpeedPin.setPwmRange(PWM_RANGE);
        rightDrivePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "rightDrive", PinState.HIGH);
        rightReversePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "rightReverse", PinState.LOW);
        rightSpeedPin = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_03, "rightSpeed", 0);
        rightSpeedPin.setPwmRange(PWM_RANGE);
        pinsProvisioned = true;
        LOGGER.info("GPIO pins have been provisioned");
    }

    @Override
    public CarControlProtos.CarData getCurrentData() {
        return CarControlProtos.CarData.newBuilder()
                .setTime(System.currentTimeMillis())
                .setHappy(true)
                .build();
    }

    @Override
    public void handleCommand(CarControlProtos.CarControllerCommand command) {
        LOGGER.debug(String.format("Received car command: throttle(%s) steer(%s)", command.getThrottle(), command.getSteer()));
        int throttle = (int)Math.round((double)command.getThrottle() * THROTTLE_MULTIPLIER);
        int drive = throttle *
                (command.getReverse() ? -1 : 1);
        Integer newLeftDrive = drive;
        Integer newRightDrive = drive;
        if (drive != 0) {
            if (command.getSteer() < 0) {
                // left steer
                newLeftDrive = calculateDriveWithSteer(newLeftDrive, command.getSteer() * -1);
            }
            else if (command.getSteer() > 0) {
                // right steer
                newRightDrive = calculateDriveWithSteer(newLeftDrive, command.getSteer());
            }
        }
        else if (command.getSteer() != 0) {
            Integer spinDrive = calculateSpinDrive(command.getSteer());
            if (command.getSteer() < 0) {
                // left spin
                newRightDrive = spinDrive;
                newLeftDrive = -newRightDrive;
            }
            else {
                // right spin
                newLeftDrive = spinDrive;
                newRightDrive = -newLeftDrive;
            }
        }

        setDriveTarget(newLeftDrive, newRightDrive);
    }

    Integer calculateDriveWithSteer(Integer drive, Integer steer) {
        double d = drive;
        double s = steer;
        double reduction = (s / Constants.MAX_STEER) * 0.8;
        return (int) Math.round(d * (1 - reduction));
    }

    private Integer calculateSpinDrive(Integer steer) {
        return (int) Math.round((double) Math.abs(steer) * SPIN_THROTTLE_REDUCTION * THROTTLE_MULTIPLIER);
    }

    long calculateDriveChangeDuration(Integer driveStart, Integer driveEnd) {
        return (long) (((double)Math.abs(driveEnd - driveStart) / 200d) * 1000d);
    }

    private void setPinValuesForDrive(int drive, GpioPinDigitalOutput drivePin, GpioPinDigitalOutput reversePin, GpioPinPwmOutput speedPin) {
        if (drive == 0) {
            drivePin.setState(PinState.LOW);
            reversePin.setState(PinState.LOW);
            speedPin.setPwm(0);
        }
        else {
            drivePin.setState(PinState.HIGH);
            reversePin.setState(drive > 0 ? PinState.LOW : PinState.HIGH);
            speedPin.setPwm((int) Math.round(Math.abs((double) drive) * PWM_RANGE_DRIVE_REDUCTION));
        }
    }

    private void setDriveTarget(Integer leftDriveTarget, Integer rightDriveTarget) {
        LOGGER.debug(String.format("Set drive target: left(%s) right(%s)", leftDriveTarget, rightDriveTarget));
        setLeftDriveTarget(leftDriveTarget);
        setRightDriveTarget(rightDriveTarget);
    }

    public Integer getLeftDrive() {
        return leftDrive;
    }

    public void setLeftDrive(Integer leftDrive) {
        this.leftDrive = leftDrive;
        LOGGER.debug(String.format("left-drive: %s", leftDrive));
        setPinValuesForDrive(leftDrive, leftDrivePin, leftReversePin, leftSpeedPin);
    }

    public Integer getRightDrive() {
        return rightDrive;
    }

    public void setRightDrive(Integer rightDrive) {
        this.rightDrive = rightDrive;
        LOGGER.debug(String.format("right-drive: %s", rightDrive));
        setPinValuesForDrive(rightDrive, rightDrivePin, rightReversePin, rightSpeedPin);
    }

    public Integer getLeftDriveTarget() {
        return leftDriveTarget;
    }

    public void setLeftDriveTarget(Integer leftDriveTarget) {
        if (this.leftDriveTarget.equals(leftDriveTarget)) return;
        if (leftDriveTimeline != null) {
            leftDriveTimeline.cancel();
        }
        this.leftDriveTarget = leftDriveTarget;
        leftDriveTimeline = new Timeline(this);
        leftDriveTimeline.addPropertyToInterpolate("leftDrive", leftDrive, leftDriveTarget);
        leftDriveTimeline.setDuration(calculateDriveChangeDuration(leftDrive, leftDriveTarget));
        leftDriveTimeline.play();
    }

    public Integer getRightDriveTarget() {
        return rightDriveTarget;
    }

    public void setRightDriveTarget(Integer rightDriveTarget) {
        if (this.rightDriveTarget.equals(rightDriveTarget)) return;
        if (rightDriveTimeline != null) {
            rightDriveTimeline.cancel();
        }
        this.rightDriveTarget = rightDriveTarget;
        rightDriveTimeline = new Timeline(this);
        rightDriveTimeline.addPropertyToInterpolate("rightDrive", rightDrive, rightDriveTarget);
        rightDriveTimeline.setDuration(calculateDriveChangeDuration(rightDrive, rightDriveTarget));
        rightDriveTimeline.play();
    }
}
