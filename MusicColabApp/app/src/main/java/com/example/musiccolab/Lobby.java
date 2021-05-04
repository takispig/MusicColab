package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Lobby extends AppCompatActivity implements View.OnClickListener {

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

    }
    @Override
    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {
        if (view.getId() == R.id.loop) {
            // do some stuff
            Toast.makeText(getApplicationContext(), "Create Loop is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.cancel_loop) {
            // do some stuff
            Toast.makeText(getApplicationContext(), "Cancel Loop is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.replay) {
            // do some stuff
            Toast.makeText(getApplicationContext(), "Replay is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.disconnect) {
            // do some stuff
            Toast.makeText(getApplicationContext(), "Disconnect is not yet implemented", Toast.LENGTH_SHORT).show();
            // redirect user after logout to the main app screen
            startActivity(new Intent(this, Login.class));
        }
    }
}