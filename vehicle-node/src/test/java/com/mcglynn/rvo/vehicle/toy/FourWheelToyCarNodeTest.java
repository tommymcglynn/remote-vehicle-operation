package com.mcglynn.rvo.vehicle.toy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FourWheelToyCarNodeTest {

    private FourWheelToyCarNode carNode;

    @Before
    public void before() {
        carNode = new FourWheelToyCarNode(0.5d, 100d);
    }

    @Test
    public void calculateDriveWithSteer() {
        assertEquals(0, (int)carNode.calculateDriveWithSteer(0, 1000));
        assertEquals(50, (int)carNode.calculateDriveWithSteer(100, 1000));
        assertEquals(25, (int)carNode.calculateDriveWithSteer(50, 1000));
        assertEquals(75, (int)carNode.calculateDriveWithSteer(100, 500));
    }

    @Test
    public void calculateDriveChangeDuration() {
        assertEquals(100, carNode.calculateDriveChangeDuration(0, 100));
        assertEquals(0, carNode.calculateDriveChangeDuration(100, 100));
        assertEquals(200, carNode.calculateDriveChangeDuration(-100, 100));
        assertEquals(30, carNode.calculateDriveChangeDuration(20, 50));
    }
}
