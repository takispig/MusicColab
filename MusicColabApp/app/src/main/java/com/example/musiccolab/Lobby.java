package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musiccolab.instruments.Drums;
import com.example.musiccolab.instruments.Instrument;
import com.example.musiccolab.instruments.InstrumentGUIBox;
import com.example.musiccolab.instruments.InstrumentType;
import com.example.musiccolab.instruments.Theremin;

import java.util.ArrayList;
import java.util.List;

import static xdroid.toaster.Toaster.toast;

public class Lobby extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private InstrumentType selectedInstrumentType;
    private Instrument selectedInstrument;

    /*
        TODO this is a placeholder for the GUI / box for every instrument in the "green box" in the Lobby
     */
    private InstrumentGUIBox instrumentGUI;

    private Instrument drums;
    private Instrument theremin;
    private List<Instrument> instrumentList = new ArrayList<Instrument>();
    private SensorManager sensorManager;
    private Sensor sensor;
    private Boolean visible= false;
    private Boolean loop= false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        // Create Listeners for the IDs: about, register

        Button loop = (Button) findViewById(R.id.loop);
        loop.setOnClickListener(this);
        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(this);
        ImageButton disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
        ImageButton more = findViewById(R.id.more_button);
        more.setOnClickListener(this);

        // TODO we should receive the selected instrument through the dropdown

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

        instrumentGUI = new InstrumentGUIBox();

        drums = new Drums(instrumentGUI, this);
        instrumentList.add(drums);

        theremin = new Theremin(instrumentGUI, this);
        instrumentList.add(theremin);

        // Drums is set as default instrument:
        selectedInstrument = drums;
        selectedInstrument.reCalibrate();
        selectedInstrumentType = selectedInstrument.getInstrumentType();
        sensor = sensorManager.getDefaultSensor(selectedInstrument.getSensorType());
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // TODO shall be triggered in the moment the user changes the selection in the drop down
    public void userSelectedInstrument(InstrumentType newSelectedInstrumentType) {
        selectedInstrumentType = newSelectedInstrumentType;
        for (Instrument instrument : instrumentList) {
            if (instrument.getInstrumentType().equals(selectedInstrumentType)) {
                selectedInstrument = instrument;
                selectedInstrument.reCalibrate();
                sensor = sensorManager.getDefaultSensor(selectedInstrument.getSensorType());
            }
        }
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
        if (view.getId() == R.id.loop) {
            Button x=findViewById(R.id.loop);
            loop=!loop;
            x.setText(loop ? "Stop  Loop":"Start Loop");

        } else if (view.getId() == R.id.calibrate) {
            // do some stuff
            selectedInstrument.reCalibrate();
        } else if (view.getId() == R.id.disconnect) {
            new LogoutThread().start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CommunicationHandling.confirmation==2){
                // reset the sensitive user data after logout
                CommunicationHandling.userName = "";
                CommunicationHandling.email = "";
                CommunicationHandling.password = "";
                CommunicationHandling.confirmation = 0;
                startActivity(new Intent(this, Login.class));
            }else if (CommunicationHandling.confirmation==0){
                toast("Connection timeout");
            } else if (CommunicationHandling.confirmation == 12) {
                toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
            }
        } else if (view.getId() == R.id.more_button) {
            ConstraintLayout info = findViewById(R.id.info);
            visible=!visible;
            info.setVisibility(visible ? View.VISIBLE:View.GONE);

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO does this work for Touch as well?
        // TODO only if user is "recording" etc
        selectedInstrument.action(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}