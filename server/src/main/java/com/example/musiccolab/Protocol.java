package main.java.com.example.musiccolab;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;

public class Protocol {
    final private short protocolName = 12845;
    SocketAddress playerAddress;
    final private List<Short> codesList = new ArrayList<Short>();

    final private int login = 1;
    final private int logout = 2;
    final private int register = 3;
    final private int createLobby = 4;
    final private int joinLobby = 5;
    final private int leaveLobby = 6;
    final private int tone = 7;
    final private int gameStart = 8;
    final private int gameEnd = 9;
    final private int gameRestart = 10;

    private short action;
    private short responseAction;

    private byte userNameSize;
    private byte emailSize;
    private byte passwordSize;



    final private String[][] responsesArray = { {"main.java.com.example.musiccolab.Client logged in", "error"},
                                          {"main.java.com.example.musiccolab.Client logged out", "error"},
                                          {"main.java.com.example.musiccolab.Client registered", "error"},};

    public Protocol(){
        for(short index = 1; index < 11; index++)
            codesList.add(index);
    }


    /**
     * @return: -1 if protocol name is wrong.
     * -2 if Action is not known.
     * else size of Data.
     */
    public short[] analyseMainBuffer(Charset messageCharset, SocketChannel clientChannel) {
        short[] temp;
        ByteBuffer mainBuffer;

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
                if (codesList.contains(action)) {
                    mainBuffer.clear();

                    clientChannel.read(mainBuffer);
                    mainBuffer.flip();
                    temp = new short[]{action, getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))};
                    return temp;

                } else {
                    return new short[]{action, -2};
                }
            } else {
                if(nameOfProtocol == -3)
                    return new short[]{action, -3};
                return new short[]{action, -1};
            }
        }
        catch (IOException e){
            return new short[]{action, -3};
        }
    }

    private void readSizes(Charset messageCharset, SocketChannel clientChannel) throws IOException {

        ByteBuffer loginSystemBuffer = ByteBuffer.allocate(1);
        if(action == register){
            clientChannel.read(loginSystemBuffer);
            loginSystemBuffer.flip();
            emailSize = messageCharset.decode(loginSystemBuffer).toString().getBytes(messageCharset)[0];
            loginSystemBuffer.clear();
        }

        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        userNameSize = messageCharset.decode(loginSystemBuffer).toString().getBytes(messageCharset)[0];
        loginSystemBuffer.clear();

        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        passwordSize = messageCharset.decode(loginSystemBuffer).toString().getBytes(messageCharset)[0];
        loginSystemBuffer.clear();
    }

    private void parseBufferForLoginSystem(Charset messageCharset, SocketChannel clientChannel) throws IOException {

        String username, password, email = "";
        boolean checkResponse;

        readSizes(messageCharset, clientChannel);
        ByteBuffer loginSystemBuffer;

        if(action == register){
            loginSystemBuffer = ByteBuffer.allocate(emailSize);
            clientChannel.read(loginSystemBuffer);
            loginSystemBuffer.flip();
            email = messageCharset.decode(loginSystemBuffer).toString();
            loginSystemBuffer.clear();
        }

        loginSystemBuffer = ByteBuffer.allocate(userNameSize);
        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        username = messageCharset.decode(loginSystemBuffer).toString();
        loginSystemBuffer.clear();

        loginSystemBuffer = ByteBuffer.allocate(passwordSize);
        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        password = messageCharset.decode(loginSystemBuffer).toString();
        loginSystemBuffer.clear();

        if(action == register) {
            try {
                checkResponse = LoginSystem.register(username, email, password);
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("main.java.com.example.musiccolab.Client is registered.");
            } catch (SQLException e) {
                checkResponse = false;
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("ERROR: SQL");
            } catch (ClassNotFoundException e) {
                checkResponse = false;
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("ERROR: ClassNotFound");
            }
        }
        else if(action == login) {
            try {
                checkResponse = LoginSystem.login(username, password, clientChannel);
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("main.java.com.example.musiccolab.Client is logged in.");
            } catch (SQLException e) {
                checkResponse = false;
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("ERROR: SQL");
            } catch (ClassNotFoundException e) {
                checkResponse = false;
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("ERROR: ClassNotFound");
            }
        }
        else if(LoginSystem.getPlayerByChannel(clientChannel) != null){
            try {
                checkResponse = LoginSystem.logout(username, password);
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("main.java.com.example.musiccolab.Client is logged out.");
            } catch (SQLException e){
                checkResponse = false;
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("ERROR: SQL");
            } catch (ClassNotFoundException e){
                checkResponse = false;
                sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
                System.out.println("ERROR: ClassNotFound");
            }
        }
    }

    private void parseBufferForLobbyOrGame(Charset messageCharset, SocketChannel clientChannel, int lobbyNameIsSize) throws IOException {

        Player player = LoginSystem.getPlayerByChannel(clientChannel);
        ByteBuffer lobbyBuffer;

        lobbyBuffer = ByteBuffer.allocate(lobbyNameIsSize);
        clientChannel.read(lobbyBuffer);
        lobbyBuffer.flip();
        if(action == createLobby && player != null){
            String lobbyName = messageCharset.decode(lobbyBuffer).toString();
            int id = Server.createLobbyId();
            Lobby lobby = new Lobby(player, lobbyName, id);
            Server.lobbyMap.put(id,lobby);
            sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(true, lobby, " created by client."));
            System.out.println("main.java.com.example.musiccolab.Lobby with ID " + lobby.getLobby_id() + " is for main.java.com.example.musiccolab.Client " + player.getId() + " created");
        }
        else if((action == joinLobby || action == leaveLobby) && player != null){
            int lobbyID = Integer.parseInt(messageCharset.decode(lobbyBuffer).toString());
            Lobby currentLobby = Server.lobbyMap.get(lobbyID);
            if(action == joinLobby){
                boolean checkResponse = (currentLobby != null);
                if(checkResponse)
                    checkResponse = currentLobby.addPlayer(player);
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(checkResponse, currentLobby, " --> you are in."));
                System.out.println("main.java.com.example.musiccolab.Client with ID " + player.getId() + " is now in lobby: " + currentLobby.getLobby_id());
            } else if(action == leaveLobby){
                boolean checkResponse = (currentLobby != null);
                if(checkResponse)
                    currentLobby.removePlayer(player);
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(checkResponse, currentLobby, " you are out."));
                System.out.println("main.java.com.example.musiccolab.Client with ID " + player.getId() + " is now out from lobby: " + currentLobby.getLobby_id());
            }

        }
        else if(player != null){//
            int lobbyID = Integer.parseInt(messageCharset.decode(lobbyBuffer).toString());
            Game game = new Game(Server.lobbyMap.get(lobbyID));
            responseAction = action;
            sendResponseToClient(messageCharset,clientChannel,Integer.toString(lobbyID));
        }
        lobbyBuffer.clear();
    }

    private void parseBufferForMusicJoiner(Charset messageCharset, SocketChannel clientChannel, int dataSize) throws IOException {

        ByteBuffer toneBuffer;
        MusicJoiner musicJoiner;

        toneBuffer = ByteBuffer.allocate(1);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        byte toneAction = messageCharset.decode(toneBuffer).toString().getBytes()[0];
        toneBuffer.clear();

        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        byte toneType = messageCharset.decode(toneBuffer).toString().getBytes()[0];
        toneBuffer.clear();

        toneBuffer = ByteBuffer.allocate(dataSize - 2);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        String toneData= messageCharset.decode(toneBuffer).toString();
        toneBuffer.clear();
        if (LoginSystem.getPlayerByChannel(clientChannel) != null) {
            Lobby clientLobby = Server.lobbyMap.get(LoginSystem.getPlayerByChannel(clientChannel).getLobbyId());
            musicJoiner = new MusicJoiner(clientLobby, toneAction, toneType, toneData, clientChannel);
            musicJoiner.handleToneData();
            responseAction = action;
            for (byte index = 0; index < clientLobby.getUsersNumber(); index++)
                sendResponseToClient(messageCharset, musicJoiner.getClientChannels().get(index), musicJoiner.getClientResponses().get(index));
        } else {
            //todo: ERROR
        }
    }

    public void handleAction(Charset messageCharset, SocketChannel clientChannel, int bufferSize) throws IOException {
        if(bufferSize != 0){
            playerAddress = clientChannel.getRemoteAddress();

            if(action == login || action == logout || action == register){
                parseBufferForLoginSystem(messageCharset, clientChannel);
            }
            else if(action == createLobby || action == joinLobby || action == leaveLobby ||
                    action == gameStart || action == gameEnd || action == gameRestart){
                parseBufferForLobbyOrGame(messageCharset, clientChannel, bufferSize);
            }
            else if(action == tone){
                parseBufferForMusicJoiner(messageCharset, clientChannel, bufferSize);
            }
        }
    }

    public byte[] convertShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);

        return temp;
    }

    private static short getShort(byte[] b) {
        if(b.length > 0)
            return (short) (((b[1] << 8) | b[0] & 0xff));
        else return -3;
    }

    private String getLoginSystemResponse(int action, boolean result){
        int index = result? 0:1;
        this.responseAction = (short) action;
        return responsesArray[action <= 10? action-1:action-11][index];
    }

    private String getLobbyResponse(boolean result, Lobby lobby, String additionPart){
        String message;
        if(result){
            message = "main.java.com.example.musiccolab.Lobby "+ lobby.getLobby_id() + additionPart;
            responseAction = action;
        }
        else{
            message = "either lobby is full, lobbyId is wrong or you are already in.";
            responseAction = (short) (action + 10);
        }
        return message;
    }

    public void sendResponseToClient(Charset messageCharset, SocketChannel clientChannel, String message){
        short dataLength = (short) message.length();
        ByteBuffer messageBuffer = ByteBuffer.allocate(6 + dataLength);
        messageBuffer.put(convertShortToByte(protocolName));
        messageBuffer.put(convertShortToByte(responseAction));
        messageBuffer.put(convertShortToByte(dataLength));
        messageBuffer.put(message.getBytes(messageCharset));
        messageBuffer.flip();
        try {
            clientChannel.write(messageBuffer);
            messageBuffer.clear();
            if (action == register || action == logout)
                clientChannel.close();
        }
        catch (IOException e){
            System.out.println("Error by sending message to client.");
        }
    }
}
