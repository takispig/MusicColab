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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musiccolab.instruments.Drums;
import com.example.musiccolab.instruments.Instrument;
import com.example.musiccolab.instruments.InstrumentGUIBox;
import com.example.musiccolab.instruments.InstrumentType;
import com.example.musiccolab.instruments.Piano;
import com.example.musiccolab.instruments.SensorEventAdapter;
import com.example.musiccolab.instruments.SoundPlayer;
import com.example.musiccolab.instruments.Theremin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Lobby extends AppCompatActivity implements View.OnClickListener, SensorEventListener, Serializable {

    private Instrument selectedInstrument = null;
    private final String[] instruments = {InstrumentType.THEREMIN, InstrumentType.DRUMS, InstrumentType.PIANO}; // for the drop down menu
    private SensorManager sensorManager;
    private Sensor sensor;
    private Boolean visible = false;
    private InstrumentGUIBox instrumentGUI;
    private int counter = 0;
    private int counter2 = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        // update 'more' text field in Lobby
        TextView lobby_nr = findViewById(R.id.server_number);
        //lobby_nr.setText(String.format("%s", Login.networkThread.lobbyID));
        lobby_nr.setText(String.format("%s", Login.networkThread.lobbyName));
        TextView instr = findViewById(R.id.instrument);
        instr.setText(String.format("%s", getIntent().getSerializableExtra(PreLobby.SELECTED_INSTRUMENT)));
        if (Login.networkThread.admin) {
            TextView admin_text = findViewById(R.id.admin_boolean);
            admin_text.setText(" true");
        }
        TextView usersInLobby = findViewById(R.id.members_number);
        usersInLobby.setText(String.format("%s", Login.networkThread.users));

        // Create Listeners for the IDs: about, register
        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(this);
        ImageButton disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
        ImageButton more = findViewById(R.id.more_button);
        more.setOnClickListener(this);

        SoundPlayer sp = new SoundPlayer(this);
        sp.generateToneList();
        Login.networkThread.soundPlayer = sp;
        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

        callInstrument(sp);

        // Drop-Down Menu (Spinner)
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> p = new ArrayAdapter<String>(Lobby.this, android.R.layout.simple_spinner_item, instruments);
        p.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(p);
        // set default value according to user's preLobby position
        switch (PreLobby.selectedInstrument) {
            case InstrumentType.THEREMIN:
                mySpinner.setSelection(0);
                break;
            case InstrumentType.DRUMS:
                mySpinner.setSelection(1);
                break;
            case InstrumentType.PIANO:
                mySpinner.setSelection(2);
                break;
        }

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (counter2 == 0) {
                    counter2++;
                    return;
                }
                // set the selected Instrument to the global variable SELECTED_INSTRUMENT
                if (instruments[position] == InstrumentType.THEREMIN)
                    getIntent().putExtra(PreLobby.SELECTED_INSTRUMENT, InstrumentType.THEREMIN);
                if (instruments[position] == InstrumentType.PIANO)
                    getIntent().putExtra(PreLobby.SELECTED_INSTRUMENT, InstrumentType.PIANO);
                if (instruments[position] == InstrumentType.DRUMS)
                    getIntent().putExtra(PreLobby.SELECTED_INSTRUMENT, InstrumentType.DRUMS);
                Toast.makeText(getApplicationContext(), "Instrument: " + instruments[position], Toast.LENGTH_SHORT).show();
                // refresh text in more
                TextView instr = findViewById(R.id.instrument);
                instr.setText(String.format("%s", getIntent().getSerializableExtra(PreLobby.SELECTED_INSTRUMENT)));

                callInstrument(sp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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

    private void callInstrument(SoundPlayer sp) {
        createInstrumentGUIBox();
        String selectedInstrumentFromPreLobby = (String) getIntent().getSerializableExtra(PreLobby.SELECTED_INSTRUMENT);
        switch (selectedInstrumentFromPreLobby) {
            case InstrumentType.THEREMIN:
                selectedInstrument = new Theremin(instrumentGUI, sp);
                break;
            case InstrumentType.DRUMS:
                selectedInstrument = new Drums(instrumentGUI, sp);
                break;
            case InstrumentType.PIANO:
                // delete previous text from Theremin or Drums
                TextView lobby_default_text = findViewById(R.id.iva_text_1);
                lobby_default_text.setText("");

                selectedInstrument = new Piano(instrumentGUI, sp);
                break;
        }

        selectedInstrument.reCalibrate();
        sensor = sensorManager.getDefaultSensor(selectedInstrument.getSensorType());
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // createInstrumentGUIBox();
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

        if (view.getId() == R.id.calibrate) {
            selectedInstrument.reCalibrate();
        }

        // this is the Leave Lobby function and not a Disconnect
        if (view.getId() == R.id.disconnect) {
            networkThread.action = 6;
            //networkThread.lobbyID = Login.networkThread.lobbyID;
            try {
                synchronized (Thread.currentThread()) {
                    // Set as connection timeout 2 seconds
                    Thread.currentThread().wait(2000);
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            String output = networkThread.result;
            System.out.println(output);

            System.out.println("LeaveLobby conf-code: " + networkThread.confirmation);
            if (networkThread.confirmation == 6) {
                // reset the sensitive user data after logout
                CommunicationHandling.wipeData(6, networkThread);
                toast("Logged out successfully");
                startActivity(new Intent(this, PreLobby.class));
            } else if (networkThread.confirmation == 0) {
                toast("Connection timeout");
                CommunicationHandling.wipeData(2, networkThread);
                startActivity(new Intent(this, Login.class));
            } else if (networkThread.confirmation == 16) {
                toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
            }
        }

        if (view.getId() == R.id.more_button) {
            TextView admin_text = findViewById(R.id.admin_boolean);
            admin_text.setText(Login.networkThread.admin ? "true" : "false");
            ConstraintLayout info = findViewById(R.id.info);
            visible = !visible;
            info.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            selectedInstrument.action(new SensorEventAdapter(event));
        } catch (IllegalArgumentException exception) {
            Log.e(getClass().getName(), exception.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onBackPressed() {
        CommunicationHandling networkThread = Login.networkThread;
        if (++counter == 1) {
            toast("Press again to Exit Lobby");
        } else {
            // else remove the user from lobby
            networkThread.action = 6;
            try {
                synchronized (Thread.currentThread()) {
                    // Set as connection timeout 2 seconds
                    Thread.currentThread().wait(2000);
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            String output = networkThread.result;

            System.out.println("LeaveLobby conf-code: " + networkThread.confirmation);
            if (networkThread.confirmation == 6) {
                // reset the sensitive user data after logout
                CommunicationHandling.wipeData(6, networkThread);
                toast("Logged out successfully");
                startActivity(new Intent(this, PreLobby.class));
            } else if (networkThread.confirmation == 0) {
                toast("Connection timeout");
                CommunicationHandling.wipeData(2, networkThread);
                startActivity(new Intent(this, Login.class));
            } else if (networkThread.confirmation == 16) {
                toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
            }

        }
    }
}