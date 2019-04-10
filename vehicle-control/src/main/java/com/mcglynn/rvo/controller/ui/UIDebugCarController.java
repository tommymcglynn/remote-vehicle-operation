package com.mcglynn.rvo.controller.ui;

import com.mcglynn.rvo.controller.CarController;
import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.util.Constants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class UIDebugCarController extends Parent implements Initializable, CarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIDebugCarController.class);
    private static Paint ON_COLOR = Color.web("#00FF00");
    private static Paint NEUTRAL_COLOR = Color.web("#000000");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    private boolean didAddEventListeners = false;
    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;
    private boolean isUpPressed = false;
    private boolean isDownPressed = false;
    private Set<KeyCode> pressedKeys = new HashSet<>();

    @FXML
    private Label leftLabel;
    @FXML
    private Label rightLabel;
    @FXML
    private Label upLabel;
    @FXML
    private Label downLabel;
    @FXML
    private Label carLabel;

    public UIDebugCarController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("UI did initialize");
    }

    private synchronized void addEventListeners() {
        if (!didAddEventListeners) {
            didAddEventListeners = true;
            leftLabel.getScene().addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
                pressedKeys.add(key.getCode());
                if (KeyCode.LEFT.equals(key.getCode())) {
                    setLeftPressed(true);
                }
                else if (KeyCode.RIGHT.equals(key.getCode())) {
                    setRightPressed(true);
                }
                else if (KeyCode.UP.equals(key.getCode())) {
                    setUpPressed(true);
                }
                else if (KeyCode.DOWN.equals(key.getCode())) {
                    setDownPressed(true);
                }
            });
            leftLabel.getScene().addEventHandler(KeyEvent.KEY_RELEASED, (key) -> {
                pressedKeys.remove(key.getCode());
                if (KeyCode.LEFT.equals(key.getCode())) {
                    setLeftPressed(false);
                }
                else if (KeyCode.RIGHT.equals(key.getCode())) {
                    setRightPressed(false);
                }
                else if (KeyCode.UP.equals(key.getCode())) {
                    setUpPressed(false);
                }
                else if (KeyCode.DOWN.equals(key.getCode())) {
                    setDownPressed(false);
                }
            });
        }
    }

    public void onStartClick(ActionEvent actionEvent) {
        addEventListeners();
    }

    @Override
    public CarControlProtos.CarControllerCommand getCurrentCommand() {
        double targetThrottle = 0;
        double targetBrake = 0;
        double targetSteer = 0;
        boolean targetReverse = false;
        // reverse: down
        // stop: down, up
        // reverse-right: down, right
        // reverse-left: down, left
        // forward: up
        // forward-left: up, left
        // forward-right: up, right
        // left-spin: left
        // right-spin: right
        if (pressedKeys.contains(KeyCode.DOWN)) {
            if (pressedKeys.contains(KeyCode.UP)) {
                // stop
            }
            else if (pressedKeys.contains(KeyCode.LEFT)) {
                // reverse-left
                targetThrottle = Constants.MAX_THROTTLE;
                targetSteer = -Constants.MAX_STEER;
                targetReverse = true;
            }
            else if (pressedKeys.contains(KeyCode.RIGHT)) {
                // reverse-right
                targetThrottle = Constants.MAX_THROTTLE;
                targetSteer = Constants.MAX_STEER;
                targetReverse = true;
            }
            else {
                // reverse
                targetThrottle = Constants.MAX_THROTTLE;
                targetReverse = true;
            }
        }
        else if (pressedKeys.contains(KeyCode.UP)) {
            if (pressedKeys.contains(KeyCode.LEFT)) {
                // forward-left
                targetThrottle = Constants.MAX_THROTTLE;
                targetSteer = -Constants.MAX_STEER;
            }
            else if (pressedKeys.contains(KeyCode.RIGHT)) {
                // forward-right
                targetThrottle = Constants.MAX_THROTTLE;
                targetSteer = Constants.MAX_STEER;
            }
            else {
                // forward
                targetThrottle = Constants.MAX_THROTTLE;
            }
        }
        else if (pressedKeys.contains(KeyCode.LEFT)) {
            if (pressedKeys.contains(KeyCode.RIGHT)) {
                // stop
            }
            else {
                // left-spin
                targetSteer = -Constants.MAX_STEER;
            }
        }
        else if (pressedKeys.contains(KeyCode.RIGHT)) {
            // right-spin
            targetSteer = Constants.MAX_STEER;
        }

        return CarControlProtos.CarControllerCommand.newBuilder()
                .setBrake((int) targetBrake)
                .setThrottle((int) targetThrottle)
                .setSteer((int) targetSteer)
                .setReverse(targetReverse)
                .build();
    }

    @Override
    public void handleCarData(CarControlProtos.CarData carData) {
        Platform.runLater(() -> carLabel.setText(String.format("Car: %s, happy=%s", DATE_FORMAT.format(new Date(carData.getTime())), carData.getHappy())));
    }

    private void setLabelColor(Label label, boolean isOn) {
        label.setTextFill(isOn ? ON_COLOR : NEUTRAL_COLOR);
    }

    public boolean isLeftPressed() {
        return isLeftPressed;
    }

    private void setLeftPressed(boolean leftPressed) {
        isLeftPressed = leftPressed;
        setLabelColor(leftLabel, isLeftPressed);
    }

    public boolean isRightPressed() {
        return isRightPressed;
    }

    private void setRightPressed(boolean rightPressed) {
        isRightPressed = rightPressed;
        setLabelColor(rightLabel, isRightPressed);
    }

    public boolean isUpPressed() {
        return isUpPressed;
    }

    private void setUpPressed(boolean upPressed) {
        isUpPressed = upPressed;
        setLabelColor(upLabel, isUpPressed);
    }

    public boolean isDownPressed() {
        return isDownPressed;
    }

    private void setDownPressed(boolean downPressed) {
        isDownPressed = downPressed;
        setLabelColor(downLabel, isDownPressed);
    }
}
