package com.example.musiccolab;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Client Class right now is operating just a basic communication with
 * the Server. This is just an abstract version with the basic functionalities.
 * Send:        "Hallo Server! Shall i come in?"
 * Receive:     <answer from Server>
 */

public class Client extends AppCompatActivity implements Runnable {

    String localhost = "10.0.2.2";           // localhost for android devices (finally)
    int port = 8080;                         // 3001, 8080, 1201, etc...
    Context context;

    // general constructor
    public Client(Context context, short action, String email, String userName, String password) {
        this.context = context;
        this.action = action;
        this.email = email;
        this.userName = userName;
        this.password = password;
        // took it from server team as it is
        for(short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add( (short) (index + 10));
        }
    }

    // this constructor is just for the disconnect
    public Client(short action) {
        this.action = action;
    }

    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;
    private static byte [] clientName = null;

    final private List<Short> codesList = new ArrayList<Short>();
    final private List<Short> errorCodesList = new ArrayList<Short>();
    private boolean neededAction = false;
    final private short protocolName = 12845;
    private short action;
    private String email;
    private String userName;
    private String password;
    private byte toneAction = 1;
    private byte toneType = 1;
    private String toneData = "dataExample2";
    private String lobbyName = "example";
    private String lobbyID = "0";

    private static void printUsage() {
        System.err.println("Usage: java SMTPClient <address> <port>");
    }
    public SocketChannel channel = null;
    @Override
    public void run() {
        InetSocketAddress remoteAddress = null;
        Selector selector = null;
        try {
            messageCharset = Charset.forName("US-ASCII");
        } catch(UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            System.exit(1);
        }

        decoder = messageCharset.newDecoder();

        try {
            clientName = java.net.InetAddress.getLocalHost().getHostName().getBytes(messageCharset);
        } catch (UnknownHostException e) {
            System.err.println("Cannot determine name of host. Exiting...");
            System.exit(1);
        }

        try {
            remoteAddress = new InetSocketAddress(localhost, port);
        } catch(IllegalArgumentException e) {
            printUsage();
            System.exit(1);
        } catch(SecurityException e) {
            printUsage();
            System.exit(1);
        }

        System.out.println("Connecting to server " + localhost + ":" + port);

        try {
            selector = Selector.open();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            channel.connect(remoteAddress);

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        boolean flag = true;
        while(true)
        {
            try {
                if(selector.select() == 0)
                    continue;
            } catch (IOException e) {
                e.printStackTrace();
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while(iter.hasNext()) {

                SelectionKey key = iter.next();

                try {

                    if(key.isConnectable())
                    {
                        channel = (SocketChannel) key.channel();
                        channel.finishConnect();
                    }
                    else if(key.isReadable()) {

                        ByteBuffer helloBuffer = ByteBuffer.allocate(100);
                        channel = (SocketChannel) key.channel();
                        short[] actionDataLength;
                        if (flag) {
                            channel.read(helloBuffer);
                            helloBuffer.flip();
                            System.out.println(messageCharset.decode(helloBuffer).toString());
                            helloBuffer.clear();
                            flag = false;
                        }

                        System.out.println("\nPlease enter \"0\" to be able to receive response from  server or \n" +
                                "enter an action to send it to Server: ");

                        // TODO: here was a Scanner input, but we can't use it in real life App
                        if (action > 0) {
                            sendQueryToServer(action, channel);
                            action = 0; // set action = 0 afterwards, to end infinite loop
                        } else {
                            actionDataLength = analyseResponse(messageCharset, channel);
                            ByteBuffer responseBuffer = ByteBuffer.allocate(actionDataLength[1]);
                            // actionDataLength[0] is the confirmation-code
                            if (actionDataLength[1] == -1)
                                System.out.println("Response from foreign server!");
                            else if (actionDataLength[1] == -2)
                                System.out.println("Server sent unknown action!");
                            else {
                                if (actionDataLength[0] == 1 || actionDataLength[0] == 2 || actionDataLength[0] == 3 ||
                                        actionDataLength[0] == 11 || actionDataLength[0] == 12 || actionDataLength[0] == 13) {
                                    channel.read(responseBuffer);
                                    responseBuffer.flip();
                                    System.out.println("Action: " + actionDataLength[0] + "\n" +
                                            "Data length: " + actionDataLength[1]);
                                    if (actionDataLength[0] > 10)
                                        System.out.println("Error message: " + messageCharset.decode(responseBuffer).toString());
                                    else
                                        System.out.println("Response: " + messageCharset.decode(responseBuffer).toString());
                                    if (actionDataLength[0] == 1) {
                                        // i have set "FLAG_ACTIVITY_NEW_TASK" to force the Client to open a new Activity
                                        context.startActivity(new Intent(context, PreLobby.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    }
                                    if (actionDataLength[0] == 2) {
                                        context.startActivity(new Intent(context, Login.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                        System.exit(1);
                                    }
                                    // register confirmation
                                    if (actionDataLength[0] == 3) {
                                        channel.close();
                                        channel = SocketChannel.open();
                                        channel.configureBlocking(false);
                                        channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
                                        channel.connect(remoteAddress);
                                        flag = true;
                                    }

                                } else if (actionDataLength[0] == 4 || actionDataLength[0] == 5 || actionDataLength[0] == 6 ||
                                        actionDataLength[0] == 8 || actionDataLength[0] == 9 || actionDataLength[0] == 10) {
                                    channel.read(responseBuffer);
                                    responseBuffer.flip();
                                    System.out.println("Action: " + actionDataLength[0] + "\n" +
                                            "Data length: " + actionDataLength[1]);
                                    if (actionDataLength[0] > 10)
                                        System.out.println("Error message: " + messageCharset.decode(responseBuffer).toString());
                                    else
                                        System.out.println("Response: " + messageCharset.decode(responseBuffer).toString());
                                } else {
                                    channel.read(responseBuffer);
                                    responseBuffer.flip();
                                    System.out.println("Action: " + actionDataLength[0] + "\n" +
                                            "Data length: " + actionDataLength[1]);
                                    if (actionDataLength[0] > 10)
                                        System.out.println("Error message: " + messageCharset.decode(responseBuffer).toString());
                                    else
                                        System.out.println("Response: " + messageCharset.decode(responseBuffer).toString());
                                }
                            }
                        }
                    }
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(1);
                }
            }
            iter.remove();
        }
    }

    public byte[] convertShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);

        return temp;
    }

    private static short getShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }

    public void sendQueryToServer(short action, SocketChannel channel) throws IOException {
        short dataLength;
        byte userNameLength;
        byte passwordLength;
        byte emailLength;

        String message;
        ByteBuffer buffer = null;

        // login | logout
        if(action == 1 || action == 2) {
            userNameLength = (byte) userName.length();
            passwordLength = (byte) password.length();
            message = userName+password;
            dataLength = (short) message.length();
            buffer = ByteBuffer.allocate(6 + 2 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(userNameLength);
            buffer.put(passwordLength);
            buffer.put(message.getBytes(messageCharset));

            if (action == 2) {
                // if user want to disconnect, clear the sensitive data
                this.email = null;
                this.userName = null;
                this.password = null;
            }
        }
        // register
        else if(action == 3) {
            emailLength = (byte) email.length();
            userNameLength = (byte) userName.length();
            passwordLength = (byte) password.length();
            message = email+userName+password;
            dataLength = (short) message.length();
            buffer = ByteBuffer.allocate(6 + 3 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(emailLength);
            buffer.put(userNameLength);
            buffer.put(passwordLength);
            buffer.put(message.getBytes(messageCharset));
        }
        // create lobby | join Lobby | leave Lobby | game Start | game End | game Restart
        else if(action == 4 || action == 5 || action == 6 ||
                action == 8 || action == 9 || action == 10){
            if(action == 4)
                message = lobbyName;
            else
                message = lobbyID;
            dataLength = (short) message.length();
            buffer = ByteBuffer.allocate(6 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(message.getBytes(messageCharset));
        }
        // tone
        if(action == 7){
            message = toneData;
            dataLength = (short) (message.length() + 2);
            buffer = ByteBuffer.allocate(6 + 2 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(toneAction);
            buffer.put(toneType);
            buffer.put(message.getBytes(messageCharset));
        }
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    private short[] analyseResponse(Charset messageCharset, SocketChannel clientChannel) throws IOException {
        short[] temp;

        ByteBuffer mainBuffer = ByteBuffer.allocate(2);
        clientChannel.read(mainBuffer);
        mainBuffer.flip();

        short action = 0;
        if (protocolName == getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))) {
            mainBuffer.clear();
            clientChannel.read(mainBuffer);
            mainBuffer.flip();
            action = getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));
            if (codesList.contains(action) || errorCodesList.contains(action)) {
                mainBuffer.clear();

                clientChannel.read(mainBuffer);
                mainBuffer.flip();
                temp = new short[]{action, getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))};
                return temp;

            } else {
                return new short[]{action, -2};
            }
        } else {
            return new short[]{action, -1};
        }
    }

}
