package com.example.musiccolab.instruments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import java.util.Optional;

public class Drums implements Instrument {

    public static final double GRAVITY = 9.81;
    public static final double TOLERANCE = 0.5;
    public static final double MAX_SENSOR_INTENSITY = 5.0;
    private static final String INSTRUMENT_NAME = "Drums";
    private static final String INSTRUMENT_TYPE = InstrumentType.DRUMS;
    private final InstrumentGUIBox instrumentGUI;
    private static final int DEFAULT_SENSOR = Sensor.TYPE_ACCELEROMETER;
    private final float[] lastKnownSensorValues = new float[3];
    private static final String TAG = "Drums";
    private final SoundPlayer sp;
    private Optional<Axis> axisPointingToGround = Optional.empty();

    public Drums(InstrumentGUIBox instrumentGUI, SoundPlayer sp) {
        this.instrumentGUI = instrumentGUI;
        this.sp = sp;
        instrumentGUI.setDrumsVisible();
    }

    @Override
    public void reCalibrate(SensorEvent event) {
        axisPointingToGround = Optional.of(getAxisWithGravity(event.values));
    }

    private Axis getAxisWithGravity(float[] values) {
        if (isForceWithinGravityRange(values[0])) {
            return Axis.X;
        } else if (isForceWithinGravityRange(values[1])) {
            return Axis.Y;
        } else {
            return Axis.Z;
        }
    }

    private boolean isForceWithinGravityRange(Float value) {
        if (value < 0) {
            value = value * (-1);
        }
        return value > GRAVITY - TOLERANCE && value < GRAVITY + TOLERANCE;
    }

    @Override
    public void reCalibrate() {
        axisPointingToGround = Optional.of(getAxisWithGravity(lastKnownSensorValues));
    }

    @Override
    public void action(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            Log.e(TAG, "Instrument DRUMS need Sensor of type " + Sensor.TYPE_ACCELEROMETER + " but received sensor of type " + event.sensor.getType() + "!");
        } else {
            checkAndHandleAllValues(event);
        }
    }

    private void checkAndHandleAllValues(SensorEvent event) {
        if (!axisPointingToGround.isPresent()) {
            reCalibrate(event);
        }
        lastKnownSensorValues[0] = event.values[0];
        lastKnownSensorValues[1] = event.values[1];
        lastKnownSensorValues[2] = event.values[2];
        if (axisPointingToGround.get().equals(Axis.X)) {
            checkAndHandleTwoValues(lastKnownSensorValues[1], lastKnownSensorValues[2]);
        } else if (axisPointingToGround.get().equals(Axis.Y)) {
            checkAndHandleTwoValues(lastKnownSensorValues[0], lastKnownSensorValues[2]);
        } else {
            checkAndHandleTwoValues(lastKnownSensorValues[0], lastKnownSensorValues[1]);
        }
    }

    private void checkAndHandleTwoValues(double force_1, double force_2) {
        if (!forceWithinLimits(force_2)) {
            instrumentGUI.setTextInCenter("DRUM_A");
            sp.sendToneToServer("drums0");
            instrumentGUI.setDrumsRotateLeft();
        } else if (!forceWithinLimits(force_1)) {
            if (force_1 < 0) {
                instrumentGUI.setTextInCenter("DRUM_B");
                sp.sendToneToServer("drums1");
            } else {
                instrumentGUI.setTextInCenter("DRUM_C");
                sp.sendToneToServer("drums2");
            }
            // TODO split this into two different visualisations
            instrumentGUI.setDrumsRotateRight();
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
    public String getInstrumentType() {
        return INSTRUMENT_TYPE;
    }

    @Override
    public int getSensorType() {
        return DEFAULT_SENSOR;
    }
}