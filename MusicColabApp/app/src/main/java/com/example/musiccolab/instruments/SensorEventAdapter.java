package com.example.musiccolab.instruments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class SensorEventAdapter {

    private final Sensor sensor;
    private final float[] values;

    public SensorEventAdapter(SensorEvent event) {
        this.values = event.values;
        this.sensor = event.sensor;
    }

    public SensorEventAdapter(float[] values, Sensor sensor) {
        this.values = values;
        this.sensor = sensor;
    }

    public float[] getValues() {
        return values;
    }

    public Sensor getSensor() {
        return sensor;
    }
}