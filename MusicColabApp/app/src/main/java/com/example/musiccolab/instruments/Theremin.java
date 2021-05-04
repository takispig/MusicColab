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
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.THEREMIN;
    private InstrumentGUIBox instrumentGUI;
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

            // TODO implement into instrumentGUIbox
            System.out.println("Light intensity:" + event.values[0] + " (" + max + ")");
            System.out.println("Current Note: " + toServer);
//            light.setText("Light intensity:" + event.values[0] + " (" + max + ")");
            //          note.setText("Current Note: " + toServer);

            float x = (max - 5) / 8;
            if (event.values[0] < x) {
                c.start();
                toServer = "c Theremin";
            } else if (event.values[0] < 2 * x) {
                d.start();
                toServer = "d Theremin";
            } else if (event.values[0] < 3 * x) {
                e.start();
                toServer = "e Theremin";
            } else if (event.values[0] < 4 * x) {
                f.start();
                toServer = "f Theremin";
            } else if (event.values[0] < 5 * x) {
                g.start();
                toServer = "g Theremin";
            } else if (event.values[0] < 6 * x) {
                a.start();
                toServer = "a Theremin";
            } else if (event.values[0] < 7 * x) {
                h.start();
                toServer = "h Theremin";
            } else if (event.values[0] < 8 * x) {
                c2.start();
                toServer = "c2 Theremin";
            } else {
                toServer = "0 Theremin";
            }
            // TODO Remove later, for test purposes only
            System.out.println(toServer);
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