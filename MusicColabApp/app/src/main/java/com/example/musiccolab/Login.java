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
            toast("Really? Create a new account\nIt's easier ;)");
        } else if (view.getId() == R.id.login_submit) {
            // send the email + password in the server to check authorisation
            userNameView = findViewById(R.id.email);
            passView = findViewById(R.id.password);
            userName = userNameView.getText().toString();
            password = passView.getText().toString();

            CommunicationHandling.getInstance();
            CommunicationHandling.userName = userName;
            CommunicationHandling.password = password;

            new LoginThread().start();
            try {
                Thread.sleep(1000);
                CommunicationHandling.getInstance();
                System.out.println(CommunicationHandling.confirmation);
                if (CommunicationHandling.confirmation == 1){
                    CommunicationHandling.confirmation = 0;
                    startActivity(new Intent(this, PreLobby.class));
                } else if (CommunicationHandling.confirmation == 0) {
                    toast("Connection timeout");
                } else if (CommunicationHandling.confirmation == 11) {
                    toast("Username/password wrong\nPlease try again");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (++counter == 1) toast("Press again to Exit");
        else finish();
    }
}
