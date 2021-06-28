package com.example.musiccolab.instruments;

import android.hardware.Sensor;

public class Theremin implements Instrument {

    private final SoundPlayer sp;
    private float lastSensorValue = 0;
    private float max = 0;
    private static final String INSTRUMENT_NAME = "Theremin";
    private static final String INSTRUMENT_TYPE = InstrumentType.THEREMIN;
    private final InstrumentGUIBox instrumentGUI;
    private static final int DEFAULT_SENSOR = Sensor.TYPE_LIGHT;
    String stringToDisplay;
    String lastTone="";

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
        if (event.getSensor().getType() == Sensor.TYPE_LIGHT) {
            lastSensorValue = event.getValues()[0];
            String toneToServer = "therm";
            StringBuilder sb = new StringBuilder();
            float x = (max-1) / 8;
            if (event.getValues()[0] < x) {
                stringToDisplay = "c Theremin";
                toneToServer += "0";
                instrumentGUI.setThereminAlpha(255);
            } else if (event.getValues()[0] < 2 * x) {
                stringToDisplay = "d Theremin";
                toneToServer += "1";
                instrumentGUI.setThereminAlpha(224);
            } else if (event.getValues()[0] < 3 * x) {
                stringToDisplay = "e Theremin";
                toneToServer += "2";
                instrumentGUI.setThereminAlpha(193);
            } else if (event.getValues()[0] < 4 * x) {
                stringToDisplay = "f Theremin";
                toneToServer += "3";
				instrumentGUI.setThereminAlpha(162);
            } else if (event.getValues()[0] < 5 * x) {
                stringToDisplay = "g Theremin";
                toneToServer += "4";
                instrumentGUI.setThereminAlpha(131);
            } else if (event.getValues()[0] < 6 * x) {
                stringToDisplay = "a Theremin";
                toneToServer += "5";
                instrumentGUI.setThereminAlpha(100);
            } else if (event.getValues()[0] < 7 * x) {
                stringToDisplay = "h Theremin";
                toneToServer += "6";
                instrumentGUI.setThereminAlpha(69);
            } else if (event.getValues()[0] < 8 * x) {
                stringToDisplay = "c2 Theremin";
                toneToServer += "7";
                instrumentGUI.setThereminAlpha(38);
            } else {
                stringToDisplay = "0 Theremin";
                instrumentGUI.setThereminAlpha(7);
            }
            if(!lastTone.equals(toneToServer)){
                sp.sendToneToServer(toneToServer,1);
                lastTone=toneToServer;
            }
            sb.append("Light intensity:");
            sb.append(event.getValues()[0]);
            sb.append(" (");
            sb.append(max);
            sb.append(")\n");
            sb.append("Current Note: ");
            sb.append(stringToDisplay);
            instrumentGUI.setTextInCenter(sb.toString());
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