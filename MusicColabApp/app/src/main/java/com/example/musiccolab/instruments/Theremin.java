package com.example.musiccolab.instruments;

import android.hardware.Sensor;

public class Theremin implements Instrument {

    public static final int THEREMIN_ALPHA_C = 255;
    public static final int THEREMIN_ALPHA_D = 224;
    public static final int THEREMIN_ALPHA_E = 193;
    public static final int THEREMIN_ALPHA_F = 162;
    public static final int THEREMIN_ALPHA_G = 131;
    public static final int THEREMIN_ALPHA_A = 100;
    public static final int THEREMIN_ALPHA_H = 69;
    public static final int THEREMIN_ALPHA_C2 = 38;
    public static final int THEREMIN_ALPHA_DEFAULT = 7;
    public static final String THEREMIN_SOUND_ID_PREFIX = "therm";
    public static final String THEREMIN_STOP = THEREMIN_SOUND_ID_PREFIX + "STOP";

    private final SoundPlayer sp;
    private float lastSensorValue = 0;
    private float max = 0;
    private static final String INSTRUMENT_NAME = "Theremin";
    private static final String INSTRUMENT_TYPE = InstrumentType.THEREMIN;
    private final InstrumentGUIBox instrumentGUI;
    private static final int DEFAULT_SENSOR = Sensor.TYPE_LIGHT;
    private String lastTone = THEREMIN_STOP;
    private int lastToneAction = -1;

    public Theremin(InstrumentGUIBox instrumentGUI, SoundPlayer sp) {
        this.instrumentGUI = instrumentGUI;
        this.sp = sp;
        instrumentGUI.setThereminVisible();
    }

    @Override
    public void reCalibrate(SensorEventAdapter event) {
        max = event.getValues()[0];
    }

    @Override
    public void reCalibrate() {
        max = lastSensorValue;
    }

    @Override
    public void action(SensorEventAdapter event) {
        if (event.getSensor().getType() != Sensor.TYPE_LIGHT) {
            return;
        }
        lastSensorValue = event.getValues()[0];
        String toneToServer = THEREMIN_SOUND_ID_PREFIX;
        StringBuilder sb = new StringBuilder();
        float x = (max - 1) / 8;
        String stringToDisplay;
        int toneAction = 1;
        if (lastSensorValue < x) {
            stringToDisplay = "c";
            toneToServer += "0";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_C);
        } else if (lastSensorValue < 2 * x) {
            stringToDisplay = "d";
            toneToServer += "1";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_D);
        } else if (lastSensorValue < 3 * x) {
            stringToDisplay = "e";
            toneToServer += "2";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_E);
        } else if (lastSensorValue < 4 * x) {
            stringToDisplay = "f";
            toneToServer += "3";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_F);
        } else if (lastSensorValue < 5 * x) {
            stringToDisplay = "g";
            toneToServer += "4";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_G);
        } else if (lastSensorValue < 6 * x) {
            stringToDisplay = "a";
            toneToServer += "5";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_A);
        } else if (lastSensorValue < 7 * x) {
            stringToDisplay = "h";
            toneToServer += "6";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_H);
        } else if (lastSensorValue < 8 * x) {
            stringToDisplay = "c2";
            toneToServer += "7";
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_C2);
        } else {
            stringToDisplay = "0";
            toneToServer = lastTone;
            instrumentGUI.setThereminAlpha(THEREMIN_ALPHA_DEFAULT);
            toneAction = 0;
        }
        if (!lastTone.equals(toneToServer) || (toneAction == 0 && lastToneAction != 0)) {
            lastToneAction = toneAction;
            sp.sendToneToServer(toneToServer, toneAction);
            lastTone = toneToServer;
        }

        sb.append("Light intensity:");
        sb.append(event.getValues()[0]);
        sb.append(" (");
        sb.append(max);
        sb.append(")\n");
        sb.append("Current Note: ");
        sb.append(stringToDisplay);
        sb.append(" ");
        sb.append(INSTRUMENT_NAME);
        instrumentGUI.setTextInCenter(sb.toString());
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