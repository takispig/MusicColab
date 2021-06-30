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
            // check data validity (no empty input)
            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || question.isEmpty()) { //VH - 22.06
                toast("All fields must be filled");
            }
            else {
                register();
            }
        } else if (view.getId() == R.id.aboutt) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        }
    }

    public void register(){
        CommunicationHandling networkThread = new CommunicationHandling(Thread.currentThread());
        networkThread.username = username;
        networkThread.password = password;
        networkThread.email = email;
        networkThread.question = question; //VH - 27.06
        networkThread.action = 3;
        if (networkThread.threadExist) networkThread.communicationThread.notify();
        else networkThread.start();
        try {
            // Set as connection timeout 5 seconds
            synchronized (Thread.currentThread()) {Thread.currentThread().wait(5000);}
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        if (networkThread.confirmation == 3) {
            toast("Registration Successful!");
            // send username and password to Login activity to make the login smoother
            Intent re = new Intent(this, Login.class);
            re.putExtra("username", username);
            re.putExtra("password", password);
            startActivity(re);
        } else if (networkThread.confirmation == 0) {
            CommunicationHandling.wipeData(2, networkThread);
            toast("Connection timeout");
        } else if (networkThread.confirmation == 13) {
            toast("Registration Failed\nUsername already exists");
        }
        networkThread.confirmation = 0;
    }


}