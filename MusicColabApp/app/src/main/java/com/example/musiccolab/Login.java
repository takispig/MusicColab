package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity implements View.OnClickListener {

    String email, password;
    EditText emailView, passView;

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
            // this open the Register activity in the app
            startActivity(new Intent(this, Register.class));
        } else if (view.getId() == R.id.about) {
            // send the user to about us website, or pip up a new window
            Toast.makeText(getApplicationContext(), "About is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.forgot_password) {
            // send a reset link/code to the user
            Toast.makeText(getApplicationContext(), "Forgot Password is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.login_submit) {
            // send the email + password in the server to check authorisation
            emailView = (EditText) findViewById(R.id.email);
            email = emailView.getText().toString();

            passView = (EditText) findViewById(R.id.password);
            password = passView.getText().toString();

            if (email.contentEquals("a") && password.contentEquals("a")) {
                // send the user to the PreLobby activity
                startActivity(new Intent(this, PreLobby.class));
            } else {
                // show a message that password or username is incorrect
                Toast.makeText(getApplicationContext(), "Username or Password incorrect", Toast.LENGTH_LONG).show();
            }
        }
    }
}
