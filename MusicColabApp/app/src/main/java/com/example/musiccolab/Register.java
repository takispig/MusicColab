package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import static xdroid.toaster.Toaster.toast;

public class Register extends AppCompatActivity implements View.OnClickListener {

    public static String email, password, username, question; //VH - 22.06
    EditText emailView, passView, userView, questionView;

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
            questionView = findViewById(R.id.question); //VH - 22.06
            question = questionView.getText().toString(); //VH - 22.06

            CommunicationHandling.getInstance();
            CommunicationHandling.userName = username;
            CommunicationHandling.password = password;
            CommunicationHandling.email = email;
            CommunicationHandling.question = question;

            new RegisterThread().start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CommunicationHandling.confirmation==3) {
                CommunicationHandling.confirmation = 0;
                toast("Registration Successful");
                startActivity(new Intent(this, Login.class));
            } else if (CommunicationHandling.confirmation==0) {
                toast("Connection timeout");
            } else if (CommunicationHandling.confirmation==13) {
                toast("Registration Failed\nUsername already exists");
            }

        } else if (view.getId() == R.id.aboutt) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        }
    }

}