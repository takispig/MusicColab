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

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {

    public static String email, password, username, question;
    EditText emailView, passView, userView, questionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // Create Listeners for the IDs: about, register
        Button reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(this);
        TextView about = (TextView) findViewById(R.id.aboutt);
        about.setOnClickListener(this);

    }

    @Override
    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {
        if (view.getId() == R.id.reset) {
            // get variable inputs from the app
            emailView = findViewById(R.id.email);
            email = emailView.getText().toString();
            passView = findViewById(R.id.password);
            password = passView.getText().toString();
            userView = findViewById(R.id.username);
            username = userView.getText().toString();
            questionView = findViewById(R.id.questionForgot);
            question = questionView.getText().toString();

            // check data validity (no empty input)
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()  || question.isEmpty()) {
                toast("All fields must be filled");
            }
            else {
                resetPassword();
            }

        } else if (view.getId() == R.id.aboutt) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        }
    }

    public void resetPassword(){
        CommunicationHandling networkThread = new CommunicationHandling(Thread.currentThread());
        networkThread.username = username;
        networkThread.password = password;
        networkThread.email = email;
        networkThread.question = question;
        networkThread.action = 8;
        if (networkThread.threadExist) {
            networkThread.communicationThread.notify();
        } else {
            networkThread.start();
        }
        try {
            // Set as connection timeout 5 seconds
            synchronized (Thread.currentThread()) {Thread.currentThread().wait(5000);}
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        System.out.println("Confirmation-code after in Register.java is: " + networkThread.confirmation);
        if (networkThread.confirmation == 8) {
            toast("Reset Password Successful");
            startActivity(new Intent(this, Login.class));
        } else if (networkThread.confirmation == 0) {
            toast("Connection timeout");
            CommunicationHandling.wipeData(2, networkThread);
            startActivity(new Intent(this, Login.class));
        } else if (networkThread.confirmation == 18) {
            toast("Reset Password Failed\nUsername and Email doesn't match");
        }
        networkThread.confirmation = 0;
    }

}