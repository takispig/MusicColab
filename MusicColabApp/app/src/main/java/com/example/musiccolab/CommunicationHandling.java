package com.example.musiccolab;

import com.example.musiccolab.instruments.SoundPlayer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
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
    public static final int PROTOCOL_BECAME_ADMIN = 9;
    public static final int PROTOCOL_UPDATE_LOBBY_ID_LIST = 20;
    public static final int PROTOCOL_UPDATE_USERS = 21;             // NOT YET TESTED
    public static final int PROTOCOL_MUTE_USERS = 22;             // NOT YET TESTED

    //private static final String IP = "10.0.2.2";   //130.149.80.94 // Google Server IP-Address
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
    public int userID = -1;
    public String lobbyName = null;
    public String lobbyId = null;
    public String mutedPlayer = null;
    public String question = null; //VH - 27.06
    public boolean admin = false;
    public int users = 0;
    public List<String> LobbyList = new LinkedList<>();
    public List<String> UsernameList = new LinkedList<>();
    public List<String> MuteList = new LinkedList<>();

    public byte toneAction;
    public byte toneType;
    public String data;

    public Thread communicationThread = null;
    private final Thread mainThread;
    public String result = "";
    public int confirmation;
    public boolean threadExist = false;

    private boolean connected = false;


    public CommunicationHandling(Thread thread) {
        mainThread = thread;
        for (short index = 1; index < 10; index++) {
            codesList.add(index);
            errorCodesList.add((short) (index + 10));
        }
        codesList.add((short) 20);  // add the update lobby list action
        codesList.add((short) 21);  // add the update number of users in lobby action
        codesList.add((short) 22);  // add the mute/unmute player action
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
                SelectionKey key = selectedKeys.next();
                clientChannel = (SocketChannel) key.channel();
                ByteBuffer buffer;
                if (key.isConnectable()) {
                    if (connected) {
                        try {
                            System.out.println("test");
                            clientChannel.close();
                            confirmation = action + 10;
                            synchronized (mainThread) {
                                mainThread.notify();
                            }
                            System.out.println("Server is disconnected.");
                        } catch (IOException e) {
                            System.out.println("Error with channel.close.");
                        }
                    }
                    buffer = ByteBuffer.allocate(100);
                    try {
                        clientChannel.finishConnect();
                        clientChannel.read(buffer);
                        buffer.flip();
                        System.out.println(messageCharset.decode(buffer));
                        connected = true;
                    } catch (IOException e) {
                        System.out.println("Problem with finishConnect");
                    }
                } else if (key.isReadable()) {
                    short[] actionAndDataLength = analyseMainBuffer(messageCharset, clientChannel);
                    if (actionAndDataLength[1] > 0) {
                        confirmation = actionAndDataLength[0];
                        handleAction(actionAndDataLength[0], actionAndDataLength[1]);
                    }
                    // if message length == 0 and action == 20, then just delete all Lobbies
                    else if (actionAndDataLength[1] == 0 && actionAndDataLength[0] == (short) 20) {
                        LobbyList.clear();
                    } else if (actionAndDataLength[1] == 0 && actionAndDataLength[0] == (short) 21) {
                        UsernameList.clear();
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
                sendLoginSystemMessage(action, email, username, password, question);
            } catch (IOException e) {
                System.err.println(CAN_NOT_WRITE_IN_BUFFER);
            }
        } else if (action == PROTOCOL_CREATE_LOBBY_ACTION || action == PROTOCOL_JOIN_LOBBY_ACTION || action == PROTOCOL_LEAVE_LOBBY_ACTION || action == PROTOCOL_MUTE_USERS) {
            System.out.println("We are in sendMessageByAction. Action: " + action);
            try {
                if (action == PROTOCOL_MUTE_USERS)
                    sendLobbyMessage(action, mutedPlayer);
                else {
                    if (action == PROTOCOL_LEAVE_LOBBY_ACTION)
                        sendLobbyMessage(action, lobbyId);
                    else
                        sendLobbyMessage(action, lobbyName);
                }
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
        } else if (action == PROTOCOL_BECAME_ADMIN || action == PROTOCOL_BECAME_ADMIN + 10) {
            admin = action == PROTOCOL_BECAME_ADMIN;
            System.out.println(admin);
            try {
                ByteBuffer adminBuffer = ByteBuffer.allocate(messageLength);
                clientChannel.read(adminBuffer);
                adminBuffer.flip();
                result = messageCharset.decode(adminBuffer).toString();
            } catch (IOException e) {
                System.out.println(CAN_NOT_WRITE_IN_BUFFER + " from adminBuffer.");
            }
            System.out.println(result);
        } else if (action == PROTOCOL_UPDATE_LOBBY_ID_LIST) {
            // delete the lobby IDs from the current list
            LobbyList.clear();
            // no update the list with the IDs we got from server
            try {
                // read buffer
                ByteBuffer lobbyIdBuffer = ByteBuffer.allocate(messageLength);
                clientChannel.read(lobbyIdBuffer);
                lobbyIdBuffer.flip();
                result = messageCharset.decode(lobbyIdBuffer).toString();
                // split the lobbyIDs and extract them into IdList
                String[] response = result.split(",");
                for (String s : response) {
                    if (!s.equals(""))
                        LobbyList.add(s);
                }
            } catch (IOException e) {
                System.out.println(CAN_NOT_WRITE_IN_BUFFER + " from lobbyIdBuffer (action 20).");
            }
            System.out.println(result);
        } else if (action == PROTOCOL_UPDATE_USERS) {
            try {
                System.out.println("Action: 21 (update num_users and usernames)");
                ByteBuffer num_users = ByteBuffer.allocate(messageLength);
                clientChannel.read(num_users);
                num_users.flip();
                result = messageCharset.decode(num_users).toString();
                System.out.println("Result: " + result);
                // clear previous list of users
                UsernameList.clear();
                // receive list of users
                String[] u_names = result.split(",");
                for (String u_name : u_names) {
                    if (!u_name.equals(""))
                        UsernameList.add(u_name);
                }
                // update the number of users
                users = UsernameList.size();
                System.out.println("Num_Users: " + users + ", UserNames: " + UsernameList);
            } catch (IOException e) {
                System.out.println(CAN_NOT_WRITE_IN_BUFFER + " from adminBuffer.");
            }
            System.out.println(result);
        }
    }

    /*
        --------------------------------- Lobby Functions ---------------------------------
     */
    private void sendToneToSoundPlayer() {
        if (soundPlayer != null) {
            String[] results = result.split(",");
            soundPlayer.playTone(results[0], Integer.parseInt(results[1]), Integer.parseInt(results[2]));
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
        if (data != null) {
            short dataLength = (short) (data.length());
            dataLength += 1;
            ByteBuffer buffer = ByteBuffer.allocate(6 + 1 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(toneAction);
            buffer.put(data.getBytes(messageCharset));

            buffer.flip();
            clientChannel.write(buffer);
            buffer.clear();
        }
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
                    String[] lobbyNameAndID = a[1].split("-");
                    lobbyName = lobbyNameAndID[0];
                    lobbyId = lobbyNameAndID[1];

                    if (action == PROTOCOL_JOIN_LOBBY_ACTION) {
                        users = Integer.parseInt(a[5].split(",")[1]);
                    }
                    System.out.println("Lobby: #" + lobbyName + " and #users: " + users);
                }
            } catch (IOException e) {
                System.err.println(CAN_NOT_READ_FROM_BUFFER);
            }
        } else {
            result = "There are no data.";
            mainThread.notify();
        }

    }

    private void sendLobbyMessage(short action, String lobbyIDOrUsername) throws IOException {
        System.out.println("We are in sendLobbyMessage with variables\naction = " + action + ", lobbyName/username = " + lobbyIDOrUsername);
        short dataLength;
        ByteBuffer buffer;

        dataLength = (short) lobbyIDOrUsername.length();
        buffer = ByteBuffer.allocate(6 + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        buffer.put(lobbyIDOrUsername.getBytes(messageCharset));

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
/*                    for (int index = 0; index < response.length; index++) {
                        if (index == 0)
                            result = response[index];   // first value is not a lobbyName
                        else
                            LobbyList.add(response[index]);
                    }*/
                    result = response[0];
                    userID = Integer.parseInt(response[2]);

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

    private void sendLoginSystemMessage(short action, String email, String username, String password, String question) throws IOException {
        short dataLength;
        byte emailLength, userNameLength, passwordLength, size = 2, questionLength;
        questionLength = 0;
        emailLength = 0;

        String message = "";
        ByteBuffer buffer;

        if (action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_FORGOT_PASSWORD) {
            emailLength = (byte) email.length();
            message = email;
            size = 4;
        }
        userNameLength = (byte) username.length();
        passwordLength = (byte) password.length();
        message += username + password;
        if (action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_FORGOT_PASSWORD) {
            message += question;
            questionLength = (byte) question.length();
        }
        dataLength = (short) message.length();
        buffer = ByteBuffer.allocate(6 + size + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        if (action == PROTOCOL_REGISTER_ACTION || action == PROTOCOL_FORGOT_PASSWORD) {
            buffer.put(emailLength);
            buffer.put(questionLength);
        }
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

    // function to clear up the data after logout or leaveLobby
    public static void wipeData(int action, CommunicationHandling networkThread) {
        if (action == 2) {
            networkThread.username = null;
            networkThread.email = null;
            networkThread.password = null;
            networkThread.question = null;
            try {
                networkThread.clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        networkThread.admin = false;
        networkThread.confirmation = 0;
        networkThread.lobbyName = null;
    }
}