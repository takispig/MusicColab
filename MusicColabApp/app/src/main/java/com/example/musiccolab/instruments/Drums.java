package com.example.musiccolab.instruments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Optional;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.R;

public class Drums implements Instrument {

    public static final double GRAVITY = 9.81;
    public static final double TOLERANCE = 0.5;
    public static final double MAX_SENSOR_INTENSITY = 5.0;
    private static final String INSTRUMENT_NAME = "Drums";
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.DRUMS;
    private InstrumentGUIBox instrumentGUI;
    private static final int DEFAULT_SENSOR = Sensor.TYPE_ACCELEROMETER;
    private float[] lastKnownSensorValues = new float[3];
    private static final String TAG = "Drums";
    private Optional<Axis> axisPointingToGround = Optional.empty();
    private final MediaPlayer drum_a, drum_b;

    public Drums(InstrumentGUIBox instrumentGUI, Lobby lobby) {
        this.instrumentGUI = instrumentGUI;
        drum_a = MediaPlayer.create(lobby, R.raw.drum_a);
        drum_b = MediaPlayer.create(lobby, R.raw.drum_b);
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
        if (value > GRAVITY - TOLERANCE && value < GRAVITY + TOLERANCE) {
            return true;
        } else {
            return false;
        }
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
        if (!forceWithinLimits(force_1)) {
            drum_a.start();
        } else if (!forceWithinLimits(force_2)) {
            drum_b.start();
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