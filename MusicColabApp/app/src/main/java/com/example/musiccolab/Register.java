package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Register extends AppCompatActivity implements View.OnClickListener {

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
            boolean suc = false;
            // send data to the server and wait for registration-confirmation
            // change suc accordingly
            // if successful redirect the user to login page
            if (suc == true) {
                startActivity(new Intent(this, Login.class));
            }
        } else if (view.getId() == R.id.aboutt) {
            // send the user to about us website, or pip up a new window
            System.out.println("About is not yet implemented.");
        }
    }
}