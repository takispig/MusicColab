package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;
import static xdroid.toaster.Toaster.toast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.musiccolab.instruments.Piano;


public class PreLobby extends AppCompatActivity implements View.OnClickListener {

    public String Instrument;
    String [] instruments = {"Theremin", "Keyboards", "Drums"};
    public static String lobbyName = "";
    public static int lobbyID = 0;

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
        ImageButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);
        Button create = findViewById(R.id.create);
        create.setOnClickListener(this);

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
                toast("Instrument: " + instruments[position]);
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
            findViewById(R.id.create_server_popup).setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.create) {
            EditText name = findViewById(R.id.servername);
            if (name.getText().toString().equals("")){
                toast("Please enter server name");
                return;
            }
            else lobbyName = name.getText().toString();
            findViewById(R.id.create_server_popup).setVisibility(View.GONE);
            new CreateThread().start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CommunicationHandling.finished==1){
                // this means we successfully created a lobby -> set status Connected with server #Num
                toast("Lobby Created Successfully\n");
                TextView lobby = findViewById(R.id.server_status);
                lobby.setText(String.format("Connected to Lobby #%s", lobbyID));
            }else if (CommunicationHandling.finished==0){
                toast("Connection timeout");
            }
        } else if (view.getId() == R.id.join_server) {
            // do some stuff
            toast("Join Server is not yet implemented");
        } else if (view.getId() == R.id.logout) {
            new LogoutThread().start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CommunicationHandling.finished==1){
                //startActivity(new Intent(this, Login.class));
            }else if (CommunicationHandling.finished==0){
                toast("Connection timeout");
            }
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
                toast("Not such an Instrument. Try again.");
            }
        }
    }
}