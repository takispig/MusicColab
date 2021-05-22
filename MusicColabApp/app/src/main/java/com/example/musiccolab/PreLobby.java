package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.musiccolab.Login;

import com.example.musiccolab.instruments.Piano;

import java.net.Socket;

public class PreLobby extends AppCompatActivity implements View.OnClickListener {

    public String Instrument;
    String [] instruments = {"Theremin", "Keyboards", "Drums"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_lobby);

        // Create Listeners for the IDs: about, register
        Button create_server = (Button) findViewById(R.id.create_server);
        create_server.setOnClickListener(this);
        TextView join_server = (TextView) findViewById(R.id.join_server);
        join_server.setOnClickListener(this);
        TextView connect = (TextView) findViewById(R.id.connect);
        connect.setOnClickListener(this);

        // Update the Username from the Client (data are stored from login)
        Client.getInstance();
        TextView username = (TextView) findViewById(R.id.username);
        username.setText(Client.userName);

        // Drop-Down Menu (Spinner) -> now idea what happens here, i took the pieces from some tutorials
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> p = new ArrayAdapter<String>(PreLobby.this, android.R.layout.simple_spinner_item, instruments);
        p.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(p);
        
        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Instrument = instruments[position]; // set the selected Instrument to the global variable Instrument
                Toast.makeText(getApplicationContext(), "Instrument: " + instruments[position], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {

        if (view.getId() == R.id.create_server) {
            // send a request to the server (change only action, all other are the same)
            Client.getInstance();
            Client.action = (short) 2;
            new Thread(()->Client.getInstance().run()).start();
            // check for any changes in Client...when login succeed then confirmation_code will be 1
            while (Client.confirmation_code == 0) {
                Client.getInstance();   // retrieve latest changes in Client to check again for the confirmation
                if (Client.confirmation_code == 4) {
                    // this means we successfully created a lobby -> set status Connected with server #Num
                    Toast.makeText(getApplicationContext(), "Lobby Created Successfully\n", Toast.LENGTH_LONG).show();
                    Client.getInstance();
                    TextView lobby = (TextView) findViewById(R.id.server_status);
                    lobby.setText(String.format("Connected to Lobby #%s", Client.lobbyID));
                } else if (Client.confirmation_code == 14) {
                    Toast.makeText(getApplicationContext(), "Can't create Lobby\nPlease try again", Toast.LENGTH_LONG).show();
                }
            }
            Client.confirmation_code = 0;   // reset to 0 for future operations

        } else if (view.getId() == R.id.join_server) {
            // do some stuff
            Toast.makeText(getApplicationContext(), "Join Server is not yet implemented", Toast.LENGTH_SHORT).show();

        } else if (view.getId() == R.id.connect) {
            // do some stuff
            System.out.println("Redirect to Lobby - Testing!");
            if (Instrument == "Theremin") {
                // redirect to the theremin activity
                startActivity(new Intent(this, LightsensorTestActivity.class));
            } else if (Instrument == "Drums") {
                startActivity(new Intent(this, Lobby.class));
            } else if (Instrument == "Keyboards") {
                startActivity(new Intent(this, Piano.class));
            } else {
                // may be redundant because it can never be empty (at least at this moment)
                startActivity(new Intent(this, Lobby.class));
                Toast.makeText(getApplicationContext(), "Not such an Instrument. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}