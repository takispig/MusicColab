package com.example.musiccolab.instruments;

public interface Instrument {

    /**
     * Recalibrates with new sensor data
     *
     * @param event the event containing values and sensor
     */
    void reCalibrate(SensorEventAdapter event);

    /**
     * Recalibrates with the last received sensor data
     */
    void reCalibrate();

    /**
     * Receives the SensorEvent and plays the instrument by the given data
     *
     * @param event the event containing values and sensor
     */
    void action(SensorEventAdapter event);

    /**
     * Returns the name of the instrument
     *
     * @return instrumentName
     */
    String getInstrumentName();

    /**
     * Returns the type of instrument
     *
     * @return instrumentType
     */
    String getInstrumentType();

    /**
     * Returns the type of sensor for this instrument
     * @return sensorType;
     */
    int getSensorType();
}