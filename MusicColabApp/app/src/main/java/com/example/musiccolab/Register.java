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

            // send data to the server and wait for registration-confirmation
            new Thread(this::register).start();
            // check confirmation
            if (suc) {
                startActivity(new Intent(this, Login.class));
                Toast.makeText(getApplicationContext(), "Successfully registered", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("Registration Failed!\n");
                Toast.makeText(getApplicationContext(), "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.aboutt) {
            // send the user to about us website, or pip up a new window
            startActivity(new Intent(this, About.class));
        }
    }

    // register -> not tested yet
    private void register() {
        try {
            // create socket and output stream
            Socket socket = new Socket(localhost, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // create all necessary variables
            byte emailLength = (byte) email.length();
            byte pswdLength = (byte) password.length();
            byte usernameLength = (byte) username.length();
            String message = email + password + username;
            short dataLength = (short) message.length();
            ByteBuffer buffer = ByteBuffer.allocate(6 + 3 + dataLength);
            // put all the data into the buffer to prepare them for sending
            buffer.put((byte) 12845);   // set protocol name
            buffer.put((byte) 2);       // action 2 -> register
            buffer.put((byte) dataLength);
            buffer.put(emailLength);
            buffer.put(pswdLength);
            buffer.put(message.getBytes(Charset.forName("US-ASCII")));
            // send data to server
            out.println(buffer);
            // receive respond
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String answer = in.readLine();
            System.out.println(answer);
            if (answer.charAt(1) == (byte) 12) {    // 12 is Register-confirmation
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Server's response:\n" + answer, Toast.LENGTH_LONG).show());
                suc = true;
            } else {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed\nServer's response:\n" + answer, Toast.LENGTH_LONG).show());
            }
        } catch (IOException eo) {
            eo.printStackTrace();
        }
    }
}