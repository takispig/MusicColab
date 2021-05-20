package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Register extends AppCompatActivity implements View.OnClickListener {

    String email, password, username;
    EditText emailView, passView, userView;
    String localhost = "10.0.2.2";
    int port = 3001;
    boolean suc = false;

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
            emailView = (EditText) findViewById(R.id.email);
            email = emailView.getText().toString();
            passView = (EditText) findViewById(R.id.password);
            password = passView.getText().toString();
            userView = (EditText) findViewById(R.id.username);
            username = userView.getText().toString();

            // call Client() constructor ny passing the suitable arguments -> action=1 (login)
            new Thread(new Client(getApplicationContext(), (short) 3, email, username, password)).start();

        } else if (view.getId() == R.id.aboutt) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        }
    }

}