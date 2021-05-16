package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musiccolab.instruments.Drums;
import com.example.musiccolab.instruments.Instrument;
import com.example.musiccolab.instruments.InstrumentGUIBox;
import com.example.musiccolab.instruments.InstrumentType;
import com.example.musiccolab.instruments.Theremin;

import java.util.ArrayList;
import java.util.List;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        // Create Listeners for the IDs: about, register

        Button loop = (Button) findViewById(R.id.loop);
        loop.setOnClickListener(this);
        TextView cancel_loop = (TextView) findViewById(R.id.cancel_loop);
        cancel_loop.setOnClickListener(this);
        TextView replay = (TextView) findViewById(R.id.replay);
        replay.setOnClickListener(this);
        TextView disconnect = (TextView) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);

        // TODO we should receive the selected instrument through the dropdown

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

        instrumentGUI = new InstrumentGUIBox();

        drums = new Drums(instrumentGUI);
        instrumentList.add(drums);

        theremin = new Theremin(instrumentGUI, this);
        instrumentList.add(theremin);

        // Theremin is set as default instrument:
        selectedInstrument = theremin;
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
            // do some stuff
            Toast.makeText(getApplicationContext(), "Create Loop is not yet implemented", Toast.LENGTH_SHORT).show();

            // TODO Create and implement a "ReCalibrate" button and move this method to it
            selectedInstrument.reCalibrate();

        } else if (view.getId() == R.id.cancel_loop) {
            // do some stuff
            Toast.makeText(getApplicationContext(), "Cancel Loop is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.replay) {
            // do some stuff
            Toast.makeText(getApplicationContext(), "Replay is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.disconnect) {
            // do some stuff

            // redirect user after logout to the main app screen
            startActivity(new Intent(this, Login.class));
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