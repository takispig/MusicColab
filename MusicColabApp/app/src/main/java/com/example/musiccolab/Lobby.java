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
    private int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        // update 'more' text field in Lobby
        TextView lobby_nr = findViewById(R.id.server_number);
        lobby_nr.setText(String.format("%s", Login.networkThread.lobbyID));
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
        CommunicationHandling networkThread = Login.networkThread;

        if (view.getId() == R.id.loop) {
            Button x = findViewById(R.id.loop);
            loop = !loop;
            x.setText(loop ? "Stop  Loop" : "Start Loop");
        }

        if (view.getId() == R.id.calibrate) {
            selectedInstrument.reCalibrate();
        }

        // this is the Leave Lobby function and not a Disconnect
        if (view.getId() == R.id.disconnect) {
            networkThread.action = 6;
            networkThread.lobbyID = PreLobby.lobbyID;
            try {
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait();
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            String output = networkThread.result;

            System.out.println("LeaveLobby conf-code: " + networkThread.confirmation);
            if (networkThread.confirmation==6){
                // reset the sensitive user data after logout
                networkThread.confirmation = 0;
                networkThread.lobbyID = -1;
                networkThread.lobbyName = null;
                toast("Logged out successfully");
                startActivity(new Intent(this, PreLobby.class));
            }else if (networkThread.confirmation==0){
                toast("Connection timeout");
            } else if (networkThread.confirmation == 16) {
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

    @Override
    public void onBackPressed() {
        CommunicationHandling networkThread = Login.networkThread;
        if (++counter == 1) {
            toast("Press again to Exit Lobby");
        }
        else {
            // else remove the user from lobby
            networkThread.action = 6;
            try {
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait();
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            String output = networkThread.result;

            System.out.println("LeaveLobby conf-code: " + networkThread.confirmation);
            if (networkThread.confirmation==6){
                // reset the sensitive user data after logout
                networkThread.lobbyID = -1;
                networkThread.lobbyName = null;
                toast("Logged out successfully");
                startActivity(new Intent(this, PreLobby.class));
            }else if (networkThread.confirmation==0){
                toast("Connection timeout");
            } else if (networkThread.confirmation == 16) {
                toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
            }
        }
    }
}