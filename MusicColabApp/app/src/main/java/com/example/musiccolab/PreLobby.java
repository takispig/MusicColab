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

    public static String SELECTED_INSTRUMENT = "selectedInstrument";
    public static String selectedInstrument = null;
    private final String[] instruments = {InstrumentType.THEREMIN, InstrumentType.DRUMS, InstrumentType.PIANO};
    public static String lobbyName = null;
    private int counter = 0;
    CommunicationHandling networkThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        networkThread = Login.networkThread;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_lobby);

        // Create Listeners for the IDs: about, register
        Button create_server = (Button) findViewById(R.id.create_server);
        create_server.setOnClickListener(this);
        TextView join_server = (TextView) findViewById(R.id.join_server);
        join_server.setOnClickListener(this);
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
        TextView username = (TextView) findViewById(R.id.username);
        username.setText(networkThread.username);
        getSpinner();

    }


    public void getSpinner(){
        // Drop-Down Menu (Spinner) -> now idea what happens here, i took the pieces from some tutorials
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> p = new ArrayAdapter<String>(PreLobby.this, android.R.layout.simple_spinner_item, instruments);
        p.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(p);

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedInstrument = instruments[position]; // set the selected Instrument to the global variable Instrument
                // Toast.makeText(getApplicationContext(), "Instrument: " + instruments[position], Toast.LENGTH_SHORT).show();
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
            if (networkThread.lobbyName != null) {
                System.out.println("LobbyName: " + networkThread.lobbyName);
                toast("You are already connected with Lobby:\n" + networkThread.lobbyName);
                return;
            }
            findViewById(R.id.create_server_popup).setVisibility(View.VISIBLE);
        }

        if (view.getId() == R.id.create) {
            EditText name = findViewById(R.id.servername);
            System.out.println("Servername: "+name.getText().toString());
            if (name.getText().toString().equals("")){
                toast("Please enter server name");
                return;
            }
            lobbyName = name.getText().toString();
            createLobby();
        }
        if (view.getId() == R.id.cancel_create) {
            findViewById(R.id.create_server_popup).setVisibility(View.GONE);
        }
        if (view.getId() == R.id.logout) {
            logout();
        }
        join(view);
    }

    public void createLobby(){
        networkThread.lobbyName = lobbyName;
        networkThread.action = 4;
        findViewById(R.id.create_server_popup).setVisibility(View.GONE);
        try {
            // Set as connection timeout 5 seconds
            synchronized (Thread.currentThread()) {Thread.currentThread().wait(5000);}
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        System.out.println("Server's response: " + networkThread.result);
        System.out.println("LobbyName: " + lobbyName + " with conf-code : " + networkThread.confirmation);
        if (networkThread.confirmation==4) {
            // this means we successfully created a lobby -> set status Connected with server #Num
            toast("Lobby Created Successfully\n");
            networkThread.confirmation = 0;
            networkThread.admin = true;
            networkThread.users = 1;
            selectInstrAndGo();
        } else if (networkThread.confirmation == 14) {
            networkThread.lobbyName = null;
            toast("Name already exists\nPlease try another one");
        } else {
            networkThread.lobbyName = null;
            toast("Connection timeout");
            CommunicationHandling.wipeData(2, networkThread);
            startActivity(new Intent(this, Login.class));
        }
        networkThread.confirmation = 0;
    }

    public void join(View view){
        if (view.getId() == R.id.join_server) {
            if (networkThread.lobbyName != null) {
                System.out.println("LobbyName: " + networkThread.lobbyName);
                toast("You are already connected to Lobby:\n" + networkThread.lobbyName);
                return;
            }
            if (networkThread.LobbyList.isEmpty()) {
                toast("There are no active Lobbies to join\nCreate one to start rocking!");
                return;
            }
            TextView available_lobbies = findViewById(R.id.available_lobbyids);
            available_lobbies.setText(String.format("Available Lobbies to join:\n %s", networkThread.LobbyList));
            findViewById(R.id.join_server_popup).setVisibility(View.VISIBLE);
        }
        if (view.getId() == R.id.join) {
            EditText lobbyID_text = findViewById(R.id.lobbyID);
            lobbyName = lobbyID_text.getText().toString();  // new code testing
            System.out.println("LobbyName: " + lobbyName);
            if (lobbyID_text.getText().toString().equals("")){
                toast("Please enter server name");
                return;
            }
            joinPressed();
        }
        if (view.getId() == R.id.cancel_join) {
            findViewById(R.id.join_server_popup).setVisibility(View.GONE);
        }
    }

    public void joinPressed(){
        networkThread.lobbyName = lobbyName;
        networkThread.action = 5;
        findViewById(R.id.join_server_popup).setVisibility(View.GONE);
        try {
            // Set as connection timeout 5 seconds
            synchronized (Thread.currentThread()) {Thread.currentThread().wait(5000);}
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        System.out.println("LobbyName " + networkThread.lobbyName + " with conf-code: " + networkThread.confirmation);
        if (networkThread.confirmation==5) {
            // this means we successfully created a lobby -> set status Connected with server #Num
            toast("You joined Lobby:\n" + networkThread.lobbyName);
            networkThread.confirmation = 0;
            selectInstrAndGo();
        } else if (networkThread.confirmation == 15) {
            networkThread.lobbyName = null;
            toast("Error while Joining the Lobby\nIs the ID correct?");
        } else {
            networkThread.lobbyName = null;
            toast("Connection timeout");
            CommunicationHandling.wipeData(2, networkThread);
            startActivity(new Intent(this, Login.class));
        }
        networkThread.confirmation = 0;
    }

    public void logout(){
        networkThread.action = 2;
        try {
            synchronized (Thread.currentThread()) {
                // Set as connection timeout 5 seconds
                Thread.currentThread().wait(5000);
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        String output = networkThread.result;
        if (networkThread.confirmation==2){
            // reset the sensitive user data after logout
            toast("Logged Out");
            CommunicationHandling.wipeData(2, networkThread);
            startActivity(new Intent(this, Login.class));
        }else if (networkThread.confirmation == 0){
            toast("Connection timeout - no response");
            CommunicationHandling.wipeData(2, networkThread);
            startActivity(new Intent(this, Login.class));
        } else if (networkThread.confirmation == 12) {
            toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
            networkThread.confirmation = 0;
        }
    }



    public void selectInstrAndGo() {
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
        }
    }

    @Override
    public void onBackPressed() {
        if (++counter == 1) {
            toast("Press again to Disconnect");
        }
        else {
            // else disconnect the user
            networkThread.action = 2;
            try {
                synchronized (Thread.currentThread()) {
                    // Set as connection timeout 5 seconds
                    Thread.currentThread().wait(5000);
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }

            String output = networkThread.result;
            System.out.println(output);

            if (networkThread.confirmation==2){
                // reset the sensitive user data after logout
                toast("Logged Out");
                CommunicationHandling.wipeData(2, networkThread);
                startActivity(new Intent(this, Login.class));
            }else if (networkThread.confirmation == 0){
                toast("Connection timeout - no response");
                CommunicationHandling.wipeData(2, networkThread);
                startActivity(new Intent(this, Login.class));
            } else if (networkThread.confirmation == 12) {
                toast("Couldn't Log you out\nWorst case scenario, exit the App manually");
                networkThread.confirmation = 0;
            }
        }
    }
}