package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;
import static xdroid.toaster.Toaster.toast;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.musiccolab.instruments.Drums;
import com.example.musiccolab.instruments.Instrument;
import com.example.musiccolab.instruments.InstrumentGUIBox;
import com.example.musiccolab.instruments.InstrumentType;
import com.example.musiccolab.instruments.Piano;
import com.example.musiccolab.instruments.Theremin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Lobby extends AppCompatActivity implements View.OnClickListener, SensorEventListener, Serializable {

    private Instrument selectedInstrument = null;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Boolean visible = false;
    private Boolean loop = false;
    private InstrumentGUIBox instrumentGUI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        // update 'more' text field in Lobby
        TextView lobby_nr = findViewById(R.id.server_number);
        lobby_nr.setText(String.format("%s", CommunicationHandling.lobbyID));
        TextView instr = findViewById(R.id.instrument);
        instr.setText(String.format("%s", getIntent().getSerializableExtra(PreLobby.SELECTED_INSTRUMENT)));

        // Create Listeners for the IDs: about, register
        Button loop = findViewById(R.id.loop);
        loop.setOnClickListener(this);
        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(this);
        ImageButton disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
        ImageButton more = findViewById(R.id.more_button);
        more.setOnClickListener(this);

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

        createInstrumentGUIBox();

        String selectedInstrumentFromPreLobby = (String) getIntent().getSerializableExtra(PreLobby.SELECTED_INSTRUMENT);

        switch (selectedInstrumentFromPreLobby) {
            case InstrumentType.THEREMIN:
                selectedInstrument = new Theremin(instrumentGUI, this);
                break;
            case InstrumentType.DRUMS:
                selectedInstrument = new Drums(instrumentGUI, this);
                break;
            case InstrumentType.PIANO:
                selectedInstrument = new Piano(instrumentGUI, this);
                break;
        }

        selectedInstrument.reCalibrate();
        sensor = sensorManager.getDefaultSensor(selectedInstrument.getSensorType());
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void createInstrumentGUIBox() {
        List<Integer> pianoKeys = new ArrayList<>();
        pianoKeys.add(R.id.btnC);
        pianoKeys.add(R.id.btnD);
        pianoKeys.add(R.id.btnE);
        pianoKeys.add(R.id.btnF);
        pianoKeys.add(R.id.btnG);
        pianoKeys.add(R.id.btnA);
        pianoKeys.add(R.id.btnH);
        pianoKeys.add(R.id.btnC2);
        instrumentGUI = new InstrumentGUIBox(this, R.id.iva_text_1, pianoKeys);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {
        CommunicationHandling.getInstance();

        if (view.getId() == R.id.loop) {
            Button x = findViewById(R.id.loop);
            loop = !loop;
            x.setText(loop ? "Stop  Loop" : "Start Loop");
        }

        if (view.getId() == R.id.calibrate) {
            selectedInstrument.reCalibrate();
        }

        if (view.getId() == R.id.disconnect) {
            new LogoutThread().start();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CommunicationHandling.confirmation==2){
                // reset the sensitive user data after logout
                CommunicationHandling.userName = null;
                CommunicationHandling.email = null;
                CommunicationHandling.password = null;
                CommunicationHandling.confirmation = 0;
                startActivity(new Intent(this, Login.class));
            }else if (CommunicationHandling.confirmation==0){
                toast("Connection timeout");
            } else if (CommunicationHandling.confirmation == 12) {
                toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
            }
        }

        if (view.getId() == R.id.more_button) {
            ConstraintLayout info = findViewById(R.id.info);
            visible = !visible;
            info.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        selectedInstrument.action(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}