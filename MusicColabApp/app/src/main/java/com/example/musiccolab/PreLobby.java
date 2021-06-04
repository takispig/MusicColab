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
import android.widget.Toast;
import com.example.musiccolab.Login;

import com.example.musiccolab.instruments.InstrumentType;
import com.example.musiccolab.instruments.Piano;

import java.net.Socket;

public class PreLobby extends AppCompatActivity implements View.OnClickListener {

    public static final String SELECTED_INSTRUMENT = "selectedInstrument";
    private String selectedInstrument;
    private final String[] instruments = {InstrumentType.THEREMIN, InstrumentType.DRUMS, InstrumentType.PIANO};
    public static String lobbyName = "";
    public static int lobbyID = 0;
    private int counter = 0;

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
        Button join = (Button) findViewById(R.id.join);
        join.setOnClickListener(this);
        Button cancel_create = (Button) findViewById(R.id.cancel_create);
        cancel_create.setOnClickListener(this);
        Button cancel_join = (Button) findViewById(R.id.cancel_join);
        cancel_join.setOnClickListener(this);


        // Update the Username from the CommunicationHandling Class (data are stored from login)
        CommunicationHandling.getInstance();
        TextView username = (TextView) findViewById(R.id.username);
        username.setText(CommunicationHandling.userName);

        // Drop-Down Menu (Spinner) -> now idea what happens here, i took the pieces from some tutorials
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> p = new ArrayAdapter<String>(PreLobby.this, android.R.layout.simple_spinner_item, instruments);
        p.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(p);
        
        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedInstrument = instruments[position]; // set the selected Instrument to the global variable Instrument
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
        CommunicationHandling.getInstance();

        if (view.getId() == R.id.create_server) {
            if ((CommunicationHandling.lobbyName != null) || (CommunicationHandling.lobbyID != -1)) {
                System.out.println(CommunicationHandling.lobbyName + "  " + CommunicationHandling.lobbyID);
                toast("You are already connected with a Lobby");
                return;
            }
            findViewById(R.id.create_server_popup).setVisibility(View.VISIBLE);
        }

        if (view.getId() == R.id.create) {
            System.out.println("Created has been pressed");
            EditText name = findViewById(R.id.servername);
            System.out.println("Servername: "+name.getText().toString());
            if (name.getText().toString().equals("")){
                toast("Please enter server name");
                return;
            }
            else {
                lobbyName = name.getText().toString();
                CommunicationHandling.lobbyName = lobbyName;
            }
            findViewById(R.id.create_server_popup).setVisibility(View.GONE);
            new CreateThread().start();
            try {
                Thread.sleep(300);
                System.out.println("LobbyName: " + lobbyName + " with conf-code : " + CommunicationHandling.confirmation);
                if (CommunicationHandling.confirmation==4) {
                    // this means we successfully created a lobby -> set status Connected with server #Num
                    toast("Lobby Created Successfully\n");
                    TextView status_text = findViewById(R.id.server_status);
                    status_text.setText(String.format("Connected to Lobby #%s\nLobby name: %s", CommunicationHandling.lobbyID, CommunicationHandling.lobbyName));
                } else if (CommunicationHandling.confirmation==0) {
                    CommunicationHandling.lobbyName = null;
                    toast("Connection timeout");
                } else if (CommunicationHandling.confirmation == 14) {
                    CommunicationHandling.lobbyName = null;
                    toast("Error while Creating the Lobby\nPlease try again");
                }
                CommunicationHandling.confirmation = 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (view.getId() == R.id.cancel_create) {
            findViewById(R.id.create_server_popup).setVisibility(View.GONE);
        }

        if (view.getId() == R.id.join_server) {
            if ((CommunicationHandling.lobbyName != null) || (CommunicationHandling.lobbyID != -1)) {
                System.out.println(CommunicationHandling.lobbyName + "  " + CommunicationHandling.lobbyID);
                toast("You are already connected with a Lobby");
                return;
            }
            if (CommunicationHandling.lobbyIDs.isEmpty()) {
                toast("There are no active Lobbies to join\nCreate one to start rocking");
                return;
            }
            TextView available_lobbies = findViewById(R.id.available_lobbyids);
            available_lobbies.setText(String.format("Available Lobby IDs to join:\n %s", CommunicationHandling.lobbyIDs));
            findViewById(R.id.join_server_popup).setVisibility(View.VISIBLE);
        }

        if (view.getId() == R.id.join) {
            CommunicationHandling.getInstance();
            EditText lobbyID_text = findViewById(R.id.lobbyID);
            lobbyID = Integer.parseInt(lobbyID_text.getText().toString());
            System.out.println("LobbyID: " + lobbyID);
            if (lobbyID_text.getText().toString().equals("")){
                toast("Please enter server name");
                return;
            }
            else {
                CommunicationHandling.lobbyID = lobbyID;
            }
            findViewById(R.id.join_server_popup).setVisibility(View.GONE);
            new JoinThread().start();
            try {
                Thread.sleep(300);
                System.out.println("LobbyID " + CommunicationHandling.lobbyID + " with conf-code: " + CommunicationHandling.confirmation);
                CommunicationHandling.getInstance();
                if (CommunicationHandling.confirmation==5) {
                    // this means we successfully created a lobby -> set status Connected with server #Num
                    toast("You joined Lobby #" + CommunicationHandling.lobbyID);
                    TextView lobby = findViewById(R.id.server_status);
                    lobby.setText(String.format("Connected to Lobby #%s", lobbyID));
                } else if (CommunicationHandling.confirmation==0) {
                    CommunicationHandling.lobbyID = -1;
                    toast("Connection timeout");
                } else if (CommunicationHandling.confirmation == 15) {
                    CommunicationHandling.lobbyID = -1;
                    toast("Error while Joining the Lobby\nIs the ID correct?");
                }
                CommunicationHandling.confirmation = 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (view.getId() == R.id.cancel_join) {
            findViewById(R.id.join_server_popup).setVisibility(View.GONE);
        }

        if (view.getId() == R.id.logout) {
            new LogoutThread().start();
            try {
                Thread.sleep(300);
                CommunicationHandling.getInstance();
                if (CommunicationHandling.confirmation==2){
                    // reset the sensitive user data after logout
                    CommunicationHandling.userName = null;
                    CommunicationHandling.email = null;
                    CommunicationHandling.password = null;
                    CommunicationHandling.lobbyID = -1;
                    CommunicationHandling.lobbyName = null;
                    CommunicationHandling.confirmation = 0;
                    startActivity(new Intent(this, Login.class));
                }else if (CommunicationHandling.confirmation == 0){
                    toast("Connection timeout - no response");
                } else if (CommunicationHandling.confirmation == 12) {
                    toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
                    CommunicationHandling.confirmation = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (view.getId() == R.id.connect) {
            if (CommunicationHandling.lobbyID < 0) {
                toast("You should create or join a server first");
                return;
            }
            Intent lobbyIntent = new Intent(this, Lobby.class);
            if (selectedInstrument.equals(InstrumentType.THEREMIN)) {
                lobbyIntent.putExtra(SELECTED_INSTRUMENT, InstrumentType.THEREMIN);
                startActivity(lobbyIntent);
            } else if (selectedInstrument.equals(InstrumentType.DRUMS)) {
                lobbyIntent.putExtra(SELECTED_INSTRUMENT, InstrumentType.DRUMS);
                startActivity(lobbyIntent);
            } else if (selectedInstrument.equals(InstrumentType.PIANO)) {
                lobbyIntent.putExtra(SELECTED_INSTRUMENT, InstrumentType.PIANO);
                startActivity(lobbyIntent);
            } else {
                Toast.makeText(getApplicationContext(), "No such Instrument: \"" + selectedInstrument + "\". Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (++counter == 1) {
            toast("Press again to Disconnect");
        }
        else {
            // else disconnect the user
            new LogoutThread().start();
            try {
                Thread.sleep(300);
                CommunicationHandling.getInstance();
                if (CommunicationHandling.confirmation==2){
                    // reset the sensitive user data after logout
                    CommunicationHandling.userName = null;
                    CommunicationHandling.email = null;
                    CommunicationHandling.password = null;
                    CommunicationHandling.lobbyID = -1;
                    CommunicationHandling.lobbyName = null;
                    CommunicationHandling.confirmation = 0;
                    startActivity(new Intent(this, Login.class));
                }else if (CommunicationHandling.confirmation == 0){
                    toast("Connection timeout - no response");
                } else if (CommunicationHandling.confirmation == 12) {
                    toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
                    CommunicationHandling.confirmation = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}