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

    String email, password;
    EditText emailView, passView;
    String localhost = "10.0.2.2";
    int port = 3001;
    boolean suc = false;

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

    public static Client client;
    @Override
    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {
        if (view.getId() == R.id.register) {
            // this open the Register activity in the app
            startActivity(new Intent(this, Register.class));
        } else if (view.getId() == R.id.about) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        } else if (view.getId() == R.id.forgot_password) {
            // send a reset link/code to the user
            Toast.makeText(getApplicationContext(), "Forgot Password is not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.login_submit) {
            // send the email + password in the server to check authorisation
            emailView = (EditText) findViewById(R.id.email);
            email = emailView.getText().toString();
            passView = (EditText) findViewById(R.id.password);
            password = passView.getText().toString();
            client = new Client(getApplicationContext(), (short) 1, email, email, password);
            // call Client() constructor ny passing the suitable arguments -> action=1 (login)
            new Thread(client).start();

        }
    }
}
