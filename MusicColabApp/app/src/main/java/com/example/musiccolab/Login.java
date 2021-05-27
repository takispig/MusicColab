package com.example.musiccolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Login extends AppCompatActivity implements View.OnClickListener {

    static String email = "";
    static String userName = "";
    static String password = "";
    static String ip = "192.168.178.52";
    static int port = 8080;
    EditText userNameView, passView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // uncomment ONLY IF NECESSARY -> eliminates ERRORS but make the App unresponsive
//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }

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
            Toast.makeText(getApplicationContext(), "Forgot Password is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.login_submit) {
            // send the email + password in the server to check authorisation
            userNameView = (EditText) findViewById(R.id.email);
            passView = (EditText) findViewById(R.id.password);
            userName = userNameView.getText().toString();
            password = passView.getText().toString();
            CommunicationHandling communicationHandling = new CommunicationHandling(ip,port);
            communicationHandling.login(userName,password);

/*            // fetch Client() data and modify them -> action=1 (login)
            Client.getInstance();
            Client.userName = userName;
            Client.password = password;
            Client.action = (short) 1;
            Thread loginThread = new Thread(()->Client.getInstance().run());
            loginThread.start();
            // check for any changes in Client...when login succeed then confirmation_code will be 1
            while (Client.confirmation_code == 0) {
                Client.getInstance();   // retrieve latest changes in Client to check again for the confirmation
                if (Client.confirmation_code == 1) {
                    startActivity(new Intent(this, PreLobby.class));
                } else if (Client.confirmation_code == 11) {
                    Toast.makeText(getApplicationContext(), "Login Failed\nPlease try again", Toast.LENGTH_LONG).show();
                }
            }
            Client.confirmation_code = (short) 0;   // reset to 0 for future operations
            loginThread.interrupt();*/
        }
    }
}
