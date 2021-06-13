package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import static xdroid.toaster.Toaster.toast;


public class Login extends AppCompatActivity implements View.OnClickListener {
    
    static String userName = "";
    static String password = "";
    EditText userNameView, passView;
    public static CommunicationHandling networkThread = null;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Create Listeners for the IDs: login, about, register, forgot_password
        TextView register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);
        TextView about = (TextView) findViewById(R.id.about);
        about.setOnClickListener(this);
        Button login = (Button) findViewById(R.id.login_submit);
        login.setOnClickListener(this);
        TextView forgot_password = (TextView) findViewById(R.id.forgot_password);
        forgot_password.setOnClickListener(this);

    }

    @Override
    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {
        if (view.getId() == R.id.register) {
            startActivity(new Intent(this, Register.class));
        } else if (view.getId() == R.id.about) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        } else if (view.getId() == R.id.forgot_password) {
            // send a reset link/code to the user
            toast("Really? Create a new account\nIt's easier :)");
        } else if (view.getId() == R.id.login_submit) {
            // send the email + password in the server to check authorisation
            userNameView = findViewById(R.id.email);
            passView = findViewById(R.id.password);
            userName = userNameView.getText().toString();
            password = passView.getText().toString();

            networkThread = new CommunicationHandling(Thread.currentThread());
            networkThread.username = userName;
            networkThread.password = password;
            networkThread.action = 1;

            System.out.println(networkThread.username);

            if (networkThread.threadExist) {
                networkThread.communicationThread.notify();
            } else {
                networkThread.start();
            }

            try {
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait();
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }

            String output = networkThread.result;
            //networkThread.IdList;

            System.out.println(networkThread.confirmation);
            if (networkThread.confirmation == 1){
                networkThread.confirmation = 0;
                startActivity(new Intent(this, PreLobby.class));
            } else if (networkThread.confirmation == 0) {
                toast("Connection timeout");
            } else if (networkThread.confirmation == 11) {
                toast("Username/password wrong\nPlease try again");
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (++counter == 1) toast("Press again to Exit");
        else this.finishAffinity();
    }
}
