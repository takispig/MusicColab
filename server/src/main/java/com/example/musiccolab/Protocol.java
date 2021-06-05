package main.java.com.example.musiccolab;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

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



    final private String[][] responsesArray = { {"Client logged in", "error with login"},
                                          {"Client logged out", "error with logout"},
                                          {"Client registered", "Client is already registered."},};

    public Protocol(){
        for(short index = 0; index < 11; index++)
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

    private void parseBufferForLoginSystem(Charset messageCharset, SocketChannel clientChannel)
            throws IOException {

        String username, password, email = "";
        boolean checkResponse = false;

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

        try {
            if (action == register) {
                checkResponse = LoginSystem.register(username, email, password);

            } else if (action == login) {
                checkResponse = LoginSystem.login(username, password, clientChannel);

            } else if (action == logout) {
                checkResponse = LoginSystem.getPlayerByChannel(clientChannel) != null && LoginSystem.logout(username, password);
            }
            Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " " + getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
            sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse));
            return;
        } catch (SQLException e) {
            System.out.println("ERROR: SQL");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: ClassNotFound");
        }
        Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " " + getLoginSystemResponse(action + 10, false));
        sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(action + 10, false));
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
            Main.logr.log(Level.INFO, "LOBBY " + lobby.getLobby_id() + " CREATED BY CLIENT " + playerAddress.toString());
        }
        else if((action == joinLobby || action == leaveLobby) && player != null){
            int lobbyID = Integer.parseInt(messageCharset.decode(lobbyBuffer).toString());
            Lobby currentLobby = Server.lobbyMap.get(lobbyID);
            if(action == joinLobby){
                boolean checkResponse = (currentLobby != null);
                if(checkResponse)
                    checkResponse = currentLobby.addPlayer(player);
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(checkResponse, currentLobby, " --> you are in."));
                if(checkResponse) Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " JOINED LOBBY " + currentLobby.getLobby_id());

            } else if(action == leaveLobby){
                boolean checkResponse = (currentLobby != null);
                if(checkResponse) {
                    currentLobby.removePlayer(player);
                    Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " LEFT LOBBY " + currentLobby.getLobby_id());
                }
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(checkResponse, currentLobby, " you are out."));
                if(checkResponse && currentLobby.isEmpty()) { Server.lobbyMap.remove(currentLobby.getLobby_id()); currentLobby = null; }
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

    private String getAllLobbyIds(HashMap<Integer, Lobby> lobbyMap) {
        String res = "";

        var entrySet = lobbyMap.entrySet();
        for(var k: entrySet){
            res += "," + k.getValue().getLobby_id();
        }
        return res;
    }

    public byte[] convertShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);

        return temp;
    }

    private static short getShort(byte[] b) {
        if(b.length > 0) {
            short value = (short) (((b[1] << 8) | b[0] & 0xff));
            return value;
        }
        else return -3;
    }

    private String getLoginSystemResponse(int action, boolean result){
        int index = result? 0:1;
        this.responseAction = (short) action;
        String IDs = "";
        if(action == 1) IDs = getAllLobbyIds(Server.lobbyMap);
        return responsesArray[action <= 10? action-1:action-11][index] + IDs;
    }

    private String getLobbyResponse(boolean result, Lobby lobby, String additionPart){
        String message;
        if(result){
            message = "Lobby "+ lobby.getLobby_id() + additionPart;
            responseAction = action;
        }
        else{
            message = "either lobby is full or lobbyId is wrong or you are already in.";
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
