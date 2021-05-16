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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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

//            // call login() to login the user
//            new Thread(this::login).start();
//            if (suc) System.out.println("Login was successful!\n");

            if (email.contentEquals("User123") && password.contentEquals("a")) {
                // send the user to the PreLobby activity
                startActivity(new Intent(this, PreLobby.class));
            } else {
                // show a message that password or username is incorrect
                Toast.makeText(getApplicationContext(), "Username or Password incorrect", Toast.LENGTH_LONG).show();
            }

        }
    }

    // login -> not tested yet
    private void login() {
        try {
            // create socket and output stream
            Socket socket = new Socket(localhost, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // create all necessary variables
            byte emailLength = (byte) email.length();
            byte pswdLength = (byte) password.length();
            String message = email + password;
            short dataLength = (short) message.length();
            ByteBuffer buffer = ByteBuffer.allocate(6 + 2 + dataLength);
            // put all the data into the buffer to prepare them for sending
            buffer.put((byte) 12845);   // set protocol name
            buffer.put((byte) 1);       // action 1 -> login
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
            if (answer.charAt(1) == (byte) 11) {    // 11 is Login-confirmation
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
