package com.example.musiccolab;

import android.widget.TextView;

import com.example.musiccolab.instruments.SoundPlayer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CommunicationHandling implements Runnable {

    public static final int PROTOCOL_LOGIN_ACTION = 1;
    public static final int PROTOCOL_LOGOUT_ACTION = 2;
    public static final int PROTOCOL_REGISTER_ACTION = 3;
    public static final int PROTOCOL_CREATE_LOBBY_ACTION = 4;
    public static final int PROTOCOL_JOIN_LOBBY_ACTION = 5;
    public static final int PROTOCOL_LEAVE_LOBBY_ACTION = 6;
    public static final int PROTOCOL_TONE_ACTION = 7;
    public static final int PROTOCOL_FORGOT_PASSWORD = 8;
    public static final int PROTOCOL_ADMIN_LEFT = 9;

    // private static final String IP = "35.207.116.16";    // Google Server IP-Address
    private static final String IP = "130.149.80.94";   // VM IP-Address
    private static final int port = 8080;

    public static final String CAN_NOT_READ_FROM_BUFFER = "Can not read from buffer.";
    public static final String CAN_NOT_WRITE_IN_BUFFER = "Can not write in buffer.";

    public SoundPlayer soundPlayer;
    private Charset messageCharset = null;
    //Characters = Host order -->  Network order = Byte
    private SocketChannel clientChannel = null;
    private InetSocketAddress remoteAddress = null;
    private Selector selector = null;

    final private List<Short> codesList = new ArrayList<>();
    final private List<Short> errorCodesList = new ArrayList<>();
    final private short protocolName = 12845;

    public short action = 0;
    public String email = null;
    public String username = null;
    public String password = null;
    public String lobbyName = null;
    public int lobbyID = -1;
    public boolean admin = false;
    public int users = 0;
    public List<Integer> IdList = new LinkedList<>();

    public byte toneAction;
    public byte toneType;
    public String data;

    public Thread communicationThread = null;
    private final Thread mainThread;
    public String result = "";
    public int confirmation;
    public boolean threadExist = false;


    public CommunicationHandling(Thread thread) {
        mainThread = thread;
        for (short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add((short) (index + 10));
        }
    }

    @Override
    public void run() {
        if (action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_LOGIN_ACTION || action == PROTOCOL_FORGOT_PASSWORD) {
            buildConnection();
            connectToServer();
        }
        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                System.out.println("Problem with selector");
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();
                clientChannel = (SocketChannel) key.channel();
                ByteBuffer buffer;
                if (key.isConnectable()) {
                    buffer = ByteBuffer.allocate(100);
                    try {
                        clientChannel.finishConnect();
                        clientChannel.read(buffer);
                        buffer.flip();
                        System.out.println(messageCharset.decode(buffer));
                    } catch (IOException e) {
                        System.out.println("Problem with finishConnect");
                    }
                } else if (key.isReadable()) {
                    short[] actionAndDataLength = analyseMainBuffer(messageCharset, clientChannel);
                    if (actionAndDataLength[1] > 0) {
                        confirmation = actionAndDataLength[0];
                        handleAction(actionAndDataLength[0], actionAndDataLength[1]);
                    } else {
                        try {
                            clientChannel.read(ByteBuffer.allocate(1000));
                        } catch (IOException e) {
                            System.err.println("Error with empty channel.");
                        }
                    }

                }
                selectedKeys.remove();
            }
            if (codesList.contains(action)) {
                sendMessageByAction(action);
                action = 0;
            }
        }
    }

    public void start() {
        communicationThread = new Thread(this, "secondaryThread");
        try {
            communicationThread.start();
            threadExist = true;
        } catch (Exception e) {
            System.out.println("Error with starting network thread.");
        }
    }

    private void sendMessageByAction(short action) {
        if (action == PROTOCOL_LOGIN_ACTION || action == PROTOCOL_LOGOUT_ACTION || action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_FORGOT_PASSWORD) {
            try {
                sendLoginSystemMessage(action, email, username, password);
            } catch (IOException e) {
                System.err.println(CAN_NOT_WRITE_IN_BUFFER);
            }
        } else if (action == PROTOCOL_CREATE_LOBBY_ACTION || action == PROTOCOL_JOIN_LOBBY_ACTION || action == PROTOCOL_LEAVE_LOBBY_ACTION) {
            try {
                if (action == PROTOCOL_CREATE_LOBBY_ACTION)
                    sendLobbyMessage(action, lobbyName);
                else
                    sendLobbyMessage(action, Integer.toString(lobbyID));
            } catch (IOException e) {
                System.err.println(CAN_NOT_WRITE_IN_BUFFER);
            }
        } else if (action == PROTOCOL_TONE_ACTION) {
            try {
                sendTone(action);
            } catch (IOException e) {
                System.err.println(CAN_NOT_WRITE_IN_BUFFER);
            }
        }
    }

    private void handleAction(short action, short messageLength) {
        if (action == PROTOCOL_LOGIN_ACTION || action == PROTOCOL_LOGOUT_ACTION || action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_FORGOT_PASSWORD || action == 11 || action == 12 || action == 13 || action == 18) {
            loginSystem(action, messageLength);
            synchronized (mainThread) {
                mainThread.notify();
            }
            if (confirmation == 2) {
                System.out.println(confirmation);
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error with waiting of main thread.");
                    e.printStackTrace();
                }
            }

        } else if (action == PROTOCOL_CREATE_LOBBY_ACTION || action == PROTOCOL_JOIN_LOBBY_ACTION || action == PROTOCOL_LEAVE_LOBBY_ACTION || action == 14 || action == 15 || action == 16) {
            lobby(action, messageLength);
            synchronized (mainThread) {
                mainThread.notify();
            }
        } else if (action == PROTOCOL_TONE_ACTION || action == 17) {
            getData(messageLength);
        } else if (action == PROTOCOL_ADMIN_LEFT) {
            admin=true;
        }
    }

    /*
        --------------------------------- Lobby Functions ---------------------------------
     */
    private void sendToneToSoundPlayer() {
        if (soundPlayer != null) {
            String[] results = result.split(",");
            soundPlayer.playToneFromServer(results[0]);
        }
    }

    private void getData(short dataLength) {
        if (dataLength >= 0) {
            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
            try {
                clientChannel.read(buffer);
                buffer.flip();
                result = messageCharset.decode(buffer).toString();
                sendToneToSoundPlayer();
            } catch (IOException e) {
                System.err.println(CAN_NOT_READ_FROM_BUFFER);
            }
        }
    }

    private void sendTone(short action) throws IOException {
        short dataLength = (short) (data.length());
        dataLength += 2;
        ByteBuffer buffer = ByteBuffer.allocate(6 + 2 + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        buffer.put(toneAction);
        buffer.put(toneType);
        buffer.put(data.getBytes(messageCharset));

        buffer.flip();
        clientChannel.write(buffer);
        buffer.clear();
    }

    private void lobby(short action, short dataLength) {
        if (dataLength >= 0) {
            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
            try {
                clientChannel.read(buffer);
                buffer.flip();
                result = messageCharset.decode(buffer).toString();
                System.out.println("Result in lobby: " + result);
                if (action == PROTOCOL_CREATE_LOBBY_ACTION || action == PROTOCOL_JOIN_LOBBY_ACTION) {
                    String[] a = result.split(" ");
                    lobbyID = Integer.parseInt(a[1]);
                    if (action == PROTOCOL_JOIN_LOBBY_ACTION) {
                        users = Integer.parseInt(a[5].split(",")[1]);
                    }
                    System.out.println("LobbyID: " + lobbyID + " and #users: " + users);
                }
            } catch (IOException e) {
                System.err.println(CAN_NOT_READ_FROM_BUFFER);
            }
        } else {
            result = "There are no data.";
            mainThread.notify();
        }

    }

    private void sendLobbyMessage(short action, String lobbyNameOrID) throws IOException {
        short dataLength;
        ByteBuffer buffer;

        dataLength = (short) lobbyNameOrID.length();
        buffer = ByteBuffer.allocate(6 + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        buffer.put(lobbyNameOrID.getBytes(messageCharset));

        buffer.flip();
        clientChannel.write(buffer);
        buffer.clear();
    }

    /*
        --------------------------------- Login System Functions ---------------------------------
     */
    private void loginSystem(short action, short dataLength) {
        if (dataLength >= 0) {
            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
            String[] response;
            try {
                clientChannel.read(buffer);
                buffer.flip();
                if (action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_LOGOUT_ACTION || action == PROTOCOL_FORGOT_PASSWORD || action == 13 || action == 12 || action == 18) {
                    result = messageCharset.decode(buffer).toString();
                    clientChannel.close();
                } else if (action == 1) {
                    response = messageCharset.decode(buffer).toString().split(",");
                    for (int index = 0; index < response.length; index++) {
                        if (index == 0)
                            result = response[index];
                        else
                            //IdList.add(Character.getNumericValue(response[index].charAt(0)));
                            IdList.add(Integer.parseInt(response[index]));
                    }
                } else {
                    result = messageCharset.decode(buffer).toString();
                }
            } catch (IOException e) {
                System.err.println(CAN_NOT_READ_FROM_BUFFER);
            }
        } else {
            result = "There is no data.";
        }
    }

    private void sendLoginSystemMessage(short action, String email, String username, String password) throws IOException {
        short dataLength;
        byte emailLength, userNameLength, passwordLength, size = 2;
        emailLength = 0;

        String message = "";
        ByteBuffer buffer;

        if (action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_FORGOT_PASSWORD) {
            emailLength = (byte) email.length();
            message = email;
            size = 3;
        }
        userNameLength = (byte) username.length();
        passwordLength = (byte) password.length();
        message += username + password;
        dataLength = (short) message.length();
        buffer = ByteBuffer.allocate(6 + size + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        if (action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_FORGOT_PASSWORD) buffer.put(emailLength);
        buffer.put(userNameLength);
        buffer.put(passwordLength);
        buffer.put(message.getBytes(messageCharset));

        buffer.flip();
        clientChannel.write(buffer);
        buffer.clear();
    }

    /**
     * Read header function
     *
     * @param messageCharset
     * @param clientChannel
     * @return
     */
    private short[] analyseMainBuffer(Charset messageCharset, SocketChannel clientChannel) {
        short[] temp;
        ByteBuffer mainBuffer;
        short action = 0;

        try {
            mainBuffer = ByteBuffer.allocate(2);
            clientChannel.read(mainBuffer);
            mainBuffer.flip();

            short nameOfProtocol = getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));
            if (protocolName == nameOfProtocol) {
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
                    System.err.println("Server sent unknown action.");
                    return new short[]{action, 0};
                }
            } else {
                System.err.println("Response is not from Server.");
                return new short[]{action, 0};
            }
        } catch (IOException e) {
            System.err.println("Error with reading buffer in analyseMainBuffer.");
            return new short[]{action, 0};
        }
    }

    /*
        --------------------------------- Connection Functions ---------------------------------
     */
    private void buildConnection() {

        try {
            messageCharset = StandardCharsets.US_ASCII;
        } catch (UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            return;
        }

        try {
            remoteAddress = new InetSocketAddress(IP, port);
        } catch (IllegalArgumentException | SecurityException e) {
            System.err.println("Can not connect to Server.");
            return;
        }
        //Network order = Byte --> Characters = Host order
        messageCharset.newDecoder();

        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.err.println("Error with selector.");
            return;
        }
        System.out.println("Connecting to server " + IP + ":" + port);
    }

    private void connectToServer() {
        try {
            clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            clientChannel.connect(remoteAddress);
        } catch (IOException e) {
            System.err.println("Connecting to Server failed.");
        }
    }

    /*
        --------------------------------- Side Functions ---------------------------------
     */
    private byte[] convertShortToByte(short value) {
        byte[] temp = new byte[2];
        temp[0] = (byte) (value & 0xff);
        temp[1] = (byte) ((value >> 8) & 0xff);
        return temp;
    }

    private static short getShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }
}