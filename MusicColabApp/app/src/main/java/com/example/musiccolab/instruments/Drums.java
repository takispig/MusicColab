package com.example.musiccolab.instruments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class Drums implements Instrument {

    public static final double GRAVITY = 9.81;
    public static final double TOLERANCE = 0.5;
    public static final double MIN_SENSOR_INTENSITY = TOLERANCE;
    public static final double MAX_SENSOR_INTENSITY = 5.0;
    private Axis axisPointingToGround = Axis.Y;
    private static final String INSTRUMENT_NAME = "Drums";
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.DRUMS;
    private InstrumentGUIBox instrumentGUI;
    private static final int DEFAULT_SENSOR = Sensor.TYPE_ACCELEROMETER;
    private double lastSensorXValue = 0;
    private double lastSensorYValue = 0;
    private double lastSensorZValue = 0;

    public Drums(InstrumentGUIBox instrumentGUI) {
        this.instrumentGUI = instrumentGUI;
    }

    @Override
    public void reCalibrate(SensorEvent event) {
        // TODO get the axis with gravitational force and set it as the axisPointingToGround
        axisPointingToGround = Axis.Y;
    }

    @Override
    public void reCalibrate() {
        // TODO recalibrate with last known values
    }

    @Override
    public void action(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            // TODO error handling
        } else {
            checkAndHandleAllValues(event);
        }
    }

    private void checkAndHandleAllValues(SensorEvent event) {
        lastSensorXValue = event.values[0];
        lastSensorYValue = event.values[1];
        lastSensorZValue = event.values[2];
        if (axisPointingToGround.equals(Axis.X)) {
            checkAndHandleTwoValues(lastSensorYValue, lastSensorZValue);
        } else if (axisPointingToGround.equals(Axis.Y)) {
            checkAndHandleTwoValues(lastSensorXValue, lastSensorZValue);
        } else {
            checkAndHandleTwoValues(lastSensorXValue, lastSensorYValue);
        }
    }

    private void checkAndHandleTwoValues(double force_1, double force_2) {
        // TODO es fehlt noch: note 1 und 2 sind ja festgelegt für ganz bestimmte Achsen
        if (forceWithinLimits(force_1)) {
            // TODO play note 1
        } else if (forceWithinLimits(force_2)) {
            // TODO play note 2
        }
    }

    private boolean forceWithinLimits(double x) {
        if (x < -MAX_SENSOR_INTENSITY) {
            return false;
        } else if (x > MAX_SENSOR_INTENSITY) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getInstrumentName() {
        return INSTRUMENT_NAME;
    }

    @Override
    public InstrumentType getInstrumentType() {
        return INSTRUMENT_TYPE;
    }

    @Override
    public int getSensorType() {
        return DEFAULT_SENSOR;
    }
}