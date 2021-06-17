package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import static xdroid.toaster.Toaster.toast;
import java.util.concurrent.ThreadLocalRandom;

public class Register extends AppCompatActivity implements View.OnClickListener {

    public static String email, password, username;
    EditText emailView, passView, userView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Create Listeners for the IDs: about, register
        Button register = (Button) findViewById(R.id.registerr);
        register.setOnClickListener(this);
        TextView about = (TextView) findViewById(R.id.aboutt);
        about.setOnClickListener(this);

    }

    @Override
    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {
        if (view.getId() == R.id.registerr) {
            // get variable inputs from the app
            emailView = findViewById(R.id.email);
            email = emailView.getText().toString();
            passView = findViewById(R.id.password);
            password = passView.getText().toString();
            userView = findViewById(R.id.username);
            username = userView.getText().toString();

            // check data validity (no empty input)
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                toast("All fields must be filled");
            }
            else {

                CommunicationHandling networkThread = new CommunicationHandling(Thread.currentThread());
                networkThread.username = username;
                networkThread.password = password;
                networkThread.email = email;
                networkThread.action = 3;

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

                System.out.println("Confirmation-code after in Register.java is: " + networkThread.confirmation);
                if (networkThread.confirmation == 3) {
                    toast("Registration Successful");
                    startActivity(new Intent(this, Login.class));
                } else if (networkThread.confirmation == 0) {
                    toast("Connection timeout");
                } else if (networkThread.confirmation == 13) {
                    toast("Registration Failed\nUsername already exists");
                }
                networkThread.confirmation = 0;
            }


        } else if (view.getId() == R.id.aboutt) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        }
    }

}