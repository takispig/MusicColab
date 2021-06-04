package com.example.musiccolab.instruments;

import android.hardware.SensorEvent;

public interface Instrument {

    /**
     * Recalibrates with new sensor data
     *
     * @param event
     */
    public void reCalibrate(SensorEvent event);

    /**
     * Recalibrates with the last received sensor data
     */
    public void reCalibrate();

    /**
     * Receives the SensorEvent and plays the instrument by the given data
     *
     * @param event
     */
    public void action(SensorEvent event);

    /**
     * Returns the name of the instrument
     *
     * @return instrumentName
     */
    public String getInstrumentName();

    /**
     * Returns the type of instrument
     *
     * @return instrumentType
     */
    public String getInstrumentType();

    /**
     * Returns the type of sensor for this instrument
     * @return sensorType;
     */
    public int getSensorType();
}