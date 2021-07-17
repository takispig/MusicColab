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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    private Boolean visible2 = false;
    private Boolean visible3 = false;
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

        // Create Listeners
        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(this);
        ImageButton disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
        ImageButton more = findViewById(R.id.more_button);
        more.setOnClickListener(this);
        ImageButton num_users = findViewById(R.id.users);
        num_users.setOnClickListener(this);
        ImageButton muted = findViewById(R.id.muted);
        muted.setOnClickListener(this);
        Button mute_unmute = findViewById(R.id.mute_unmute);
        mute_unmute.setOnClickListener(this);
        Button cancel = findViewById(R.id.cancel_create2);
        cancel.setOnClickListener(this);
        SoundPlayer sp = new SoundPlayer(this);
        Login.networkThread.soundPlayer = sp;
        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

        callInstrument(sp);
        getSpinner(sp);
    }

    private void getSpinner(SoundPlayer sp){
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
            public void onNothingSelected(AdapterView<?> parent) {}});
    }

    private void createInstrumentGUIBox() {
        List<Integer> pianoKeyIDs = new ArrayList<>();
        pianoKeyIDs.add(R.id.btnC);
        pianoKeyIDs.add(R.id.btnD);
        pianoKeyIDs.add(R.id.btnE);
        pianoKeyIDs.add(R.id.btnF);
        pianoKeyIDs.add(R.id.btnG);
        pianoKeyIDs.add(R.id.btnA);
        pianoKeyIDs.add(R.id.btnH);
        pianoKeyIDs.add(R.id.btnC2);
        instrumentGUI = new InstrumentGUIBox(this, R.id.iva_text_1, pianoKeyIDs);
        instrumentGUI.init();
        instrumentGUI.setScaleDownAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down));
        RotateAnimation rotateRight = new RotateAnimation(40, 45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        RotateAnimation rotateLeft = new RotateAnimation(-40, -45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        RotateAnimation rotateVert = new RotateAnimation(0, 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        instrumentGUI.createRotateAnimations(rotateRight, rotateLeft, rotateVert);
    }

    private void callInstrument(SoundPlayer sp) {
        createInstrumentGUIBox();
        String selectedInstrumentFromPreLobby = (String) getIntent().getSerializableExtra(PreLobby.SELECTED_INSTRUMENT);
        switch (selectedInstrumentFromPreLobby) {
            case InstrumentType.THEREMIN:
                selectedInstrument = new Theremin(instrumentGUI, sp);
                findViewById(R.id.calibrate).setVisibility(View.VISIBLE);
                break;
            case InstrumentType.DRUMS:
                selectedInstrument = new Drums(instrumentGUI, sp);
                findViewById(R.id.calibrate).setVisibility(View.VISIBLE);
                break;
            case InstrumentType.PIANO:
                // delete previous text from Theremin or Drums
                TextView lobby_default_text = findViewById(R.id.iva_text_1);
                lobby_default_text.setText("");
                findViewById(R.id.calibrate).setVisibility(View.INVISIBLE);

                selectedInstrument = new Piano(instrumentGUI, sp);
                break;
        }

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
        if (view.getId() == R.id.calibrate) selectedInstrument.reCalibrate();
        // this is the Leave Lobby function and not a Disconnect
        if (view.getId() == R.id.disconnect) {
            networkThread.action = 6;
            networkThread.soundPlayer.stopEverything();
            //networkThread.lobbyID = Login.networkThread.lobbyID;
            try {
                // Set as connection timeout 5 seconds
                synchronized (Thread.currentThread()){Thread.currentThread().wait(5000);}
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            System.out.println( networkThread.result);
            System.out.println("LeaveLobby conf-code: " + networkThread.confirmation);
            if (networkThread.confirmation == 6) {
                // reset the sensitive user data after logout & clear LobbyNames
                networkThread.LobbyList.clear();
                CommunicationHandling.wipeData(6, networkThread);
                toast("Left Lobby successfully");
                startActivity(new Intent(this, PreLobby.class));
            } else if (networkThread.confirmation == 0) {
                toast("Connection timeout");
                CommunicationHandling.wipeData(2, networkThread);
                startActivity(new Intent(this, Login.class));
            } else if (networkThread.confirmation == 16) {
                toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
            }
        }
        if (view.getId() == R.id.more_button) getMore();
        if (view.getId() == R.id.users) {
            // display usernames
            TextView usernames = findViewById(R.id.usernames);
            if (!networkThread.UsernameList.isEmpty()) usernames.setText(String.format("%s", networkThread.UsernameList));
            // message visibility
            visible2 = !visible2;
            ConstraintLayout userss = findViewById(R.id.users_message);
            userss.setVisibility(visible2 ? View.VISIBLE : View.GONE);
        }
        if (view.getId() == R.id.muted) {
            visible3 = !visible3;
            if (networkThread.admin) findViewById(R.id.muted_message).setVisibility(visible3 ? View.VISIBLE : View.GONE);
            else toast("You need to be Admin to mute players");
            // display muted players
            TextView m_usernames = findViewById(R.id.muted_names);
            if (!networkThread.MuteList.isEmpty()) m_usernames.setText(String.format("%s", networkThread.MuteList));
            else m_usernames.setText("-");

        }
        if (view.getId() == R.id.mute_unmute) {
            visible3 = !visible3;
            findViewById(R.id.muted_message).setVisibility(View.GONE);
            EditText d = findViewById(R.id.name_to_mute);
            String tmp_muted = d.getText().toString();
            // connect to server
            networkThread.mutedPlayer = tmp_muted;
            networkThread.action = 22;
            try {
                synchronized (Thread.currentThread()) {Thread.currentThread().wait(1);}
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            // local changes
            if (networkThread.MuteList.contains(tmp_muted)) {
                networkThread.MuteList.remove(tmp_muted);
                toast(tmp_muted + " has been unmuted");
            } else {
                networkThread.MuteList.add(tmp_muted);
                toast(tmp_muted + " has been muted");
            }

        }
        if (view.getId() == R.id.cancel_create2) {
            visible3 = !visible3;
            findViewById(R.id.muted_message).setVisibility(View.GONE);
        }
    }

    private void getMore(){
        TextView admin_text = findViewById(R.id.admin_boolean);
        admin_text.setText(Login.networkThread.admin ? "true" : "false");
        TextView usersInLobby = findViewById(R.id.members_number);
        usersInLobby.setText(String.format("%s", Login.networkThread.users));
        ConstraintLayout info = findViewById(R.id.info);
        visible = !visible;
        info.setVisibility(visible ? View.VISIBLE : View.GONE);
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
            networkThread.soundPlayer.stopEverything();
            try {
                synchronized (Thread.currentThread()) {
                    // Set as connection timeout 5 seconds
                    Thread.currentThread().wait(5000);
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            String output = networkThread.result;

            System.out.println("LeaveLobby conf-code: " + networkThread.confirmation);
            if (networkThread.confirmation == 6) {
                // reset the sensitive user data after logout & clear LobbyNames
                networkThread.LobbyList.clear();
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