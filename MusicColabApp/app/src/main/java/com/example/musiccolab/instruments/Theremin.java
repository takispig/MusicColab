package com.example.musiccolab.instruments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.MediaPlayer;
import android.widget.TextView;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.R;

public class Theremin implements Instrument {

    private float lastSensorValue = 0;
    private float max = 0;
    private static final String INSTRUMENT_NAME = "Theremin";
    private static final String INSTRUMENT_TYPE = InstrumentType.THEREMIN;
    private final InstrumentGUIBox instrumentGUI;
    private MediaPlayer c, d, e, f, g, a, h, c2;
    private Lobby lobby;
    private String toServer = "0 Theremin";
    private TextView light;
    private TextView note;
    private static final int DEFAULT_SENSOR = Sensor.TYPE_LIGHT;

    public Theremin(InstrumentGUIBox instrumentGUI, Lobby lobby) {
        this.instrumentGUI = instrumentGUI;
        this.lobby = lobby;
        c = MediaPlayer.create(lobby, R.raw.c);
        d = MediaPlayer.create(lobby, R.raw.d);
        e = MediaPlayer.create(lobby, R.raw.e);
        f = MediaPlayer.create(lobby, R.raw.f);
        g = MediaPlayer.create(lobby, R.raw.g);
        a = MediaPlayer.create(lobby, R.raw.a);
        h = MediaPlayer.create(lobby, R.raw.h);
        c2 = MediaPlayer.create(lobby, R.raw.c2);
        light = (TextView) lobby.findViewById(R.id.sensor);
        note = (TextView) lobby.findViewById(R.id.note);

        instrumentGUI.setThereminVisible();
    }

    @Override
    public void reCalibrate(SensorEvent event) {
        max = event.values[0];
    }

    @Override
    public void reCalibrate() {
        max = lastSensorValue;
    }

    @Override
    public void action(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            lastSensorValue = event.values[0];

            StringBuilder sb = new StringBuilder();
            float x = (max - 5) / 8;
            if (event.values[0] < x) {
                c.start();
                toServer = "c Theremin";
                instrumentGUI.setThereminAlpha(255);
            } else if (event.values[0] < 2 * x) {
                d.start();
                toServer = "d Theremin";
                instrumentGUI.setThereminAlpha(224);
            } else if (event.values[0] < 3 * x) {
                e.start();
                toServer = "e Theremin";
                instrumentGUI.setThereminAlpha(193);
            } else if (event.values[0] < 4 * x) {
                f.start();
                toServer = "f Theremin";
            } else if (event.values[0] < 5 * x) {
                g.start();
                toServer = "g Theremin";
                instrumentGUI.setThereminAlpha(162);
            } else if (event.values[0] < 6 * x) {
                a.start();
                toServer = "a Theremin";
            } else if (event.values[0] < 7 * x) {
                h.start();
                toServer = "h Theremin";
                instrumentGUI.setThereminAlpha(100);
            } else if (event.values[0] < 8 * x) {
                c2.start();
                toServer = "c2 Theremin";
                instrumentGUI.setThereminAlpha(69);
            } else {
                toServer = "0 Theremin";
                instrumentGUI.setThereminAlpha(38);
            }
            sb.append("Light intensity:" + event.values[0] + " (" + max + ")\n");
            sb.append("Current Note: " + toServer);
            instrumentGUI.setTextInCenter(toServer);
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