package com.example.musiccolab;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
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
    final private int passwordForgotten = 8;
    final public static int becameAdmin = 9;
    final private int mutePlayer = 22;
    final private int sendPlayersInLobby = 21;

    private short action;
    public short responseAction;

    byte userNameSize;
    byte emailSize;
    byte passwordSize;
    byte questionLength;

    String username, password, email = "";
    String lobbyName = "";

    Player player = null;
    int lobbyID;

    String toneData = "";
    boolean testCorrect = false;



    final private String[][] responsesArray = { {"Client logged in", "error with login"},
                                          {"Client logged out", "error with logout"},
                                          {"Client registered", "Client is already registered."},
                                          {"password reset","Error with rest password"},
                                          {"Client joined Lobby","Client left Lobby"}};

    public Protocol(){
        for(short index = 0; index < 11; index++){
            codesList.add(index);
        }
        codesList.add((short) 22);
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
        if(action == register || action == passwordForgotten){
            clientChannel.read(loginSystemBuffer);
            loginSystemBuffer.flip();
            emailSize = messageCharset.decode(loginSystemBuffer).toString().getBytes(messageCharset)[0];
            loginSystemBuffer.clear();

            clientChannel.read(loginSystemBuffer);
            loginSystemBuffer.flip();
            questionLength = messageCharset.decode(loginSystemBuffer).toString().getBytes(messageCharset)[0];
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


    private void parseBufferIfPasswordForgotten(Charset messageCharset, SocketChannel clientChannel, SelectionKey key) throws IOException {

        String username, email, password,question = "";
        boolean checkResponse = false;

        readSizes(messageCharset, clientChannel);
        ByteBuffer loginSystemBuffer;


        loginSystemBuffer = ByteBuffer.allocate(emailSize);
        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        email = messageCharset.decode(loginSystemBuffer).toString();
        System.out.println(email);
        loginSystemBuffer.clear();

        // read username
        loginSystemBuffer = ByteBuffer.allocate(userNameSize);
        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        username = messageCharset.decode(loginSystemBuffer).toString();
        System.out.println(username);
        loginSystemBuffer.clear();

        loginSystemBuffer = ByteBuffer.allocate(passwordSize);
        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        password = messageCharset.decode(loginSystemBuffer).toString();
        System.out.println(password);
        loginSystemBuffer.clear();

        //readSecurityQuestion
        loginSystemBuffer = ByteBuffer.allocate(questionLength);
        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        question = messageCharset.decode(loginSystemBuffer).toString();
        System.out.println("Sec:" +  question);

        try {
            checkResponse = LoginSystem.forgotPassword(username, email, password,question);
            Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " " + (checkResponse? "New Password set":"Answer wrong"));
            responseAction = (short) (checkResponse ? action : action + 10);
            sendResponseToClient(messageCharset, clientChannel, checkResponse? "New Password set":"Answer wrong");

        } catch (SQLException e) {
            System.out.println("Fehler passwort Reset");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Fehler passwort Reset");
            e.printStackTrace();
        }
        sendResponseToClient(messageCharset, clientChannel, "ERROR");

    }

    private void parseBufferForLoginSystem(Charset messageCharset, SocketChannel clientChannel, SelectionKey key) throws IOException {
        boolean checkResponse = false;
        String question ="";
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

        if(action == register){
            loginSystemBuffer = ByteBuffer.allocate(questionLength);
            clientChannel.read(loginSystemBuffer);
            loginSystemBuffer.flip();
            question = messageCharset.decode(loginSystemBuffer).toString();
            System.out.println("Sec:" +  question);
        }

        try {
            if (action == register) {
                checkResponse = LoginSystem.register(username, email, password, question);

            } else if (action == login) {
                Player player = LoginSystem.login(username, password, clientChannel);
                checkResponse = player != null;
                if(checkResponse) key.attach(player);

            } else if (action == logout) {
                checkResponse = key.attachment() != null && LoginSystem.logout(username, password);
            }
            if(playerAddress != null) Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " " + getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse, key));
            sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse ? action : action + 10, checkResponse, key)); return;
        } catch (SQLException e) {
            System.out.println("ERROR: SQL");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: ClassNotFound");
        }
        if(playerAddress != null) Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " " + getLoginSystemResponse(action + 10, false, key));
        sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(action + 10, false, key));
    }

    private void parseBufferForLobbyOrGame(Charset messageCharset, SocketChannel clientChannel, int lobbyNameIsSize, SelectionKey key) throws IOException {

        Player player = (Player) key.attachment();
        /*
            Just for testing.
            Please uncomment the fowling line if you run the tests.
        */
        //player = this.player; //For testing

        ByteBuffer lobbyBuffer;

        lobbyBuffer = ByteBuffer.allocate(lobbyNameIsSize);
        clientChannel.read(lobbyBuffer);
        lobbyBuffer.flip();
        if(action == createLobby && player != null){
            lobbyName = messageCharset.decode(lobbyBuffer).toString();
            if (!Lobby.lobbyNameExist(lobbyName)) {
                int id = Server.createLobbyId();
                Lobby lobby = new Lobby(player, lobbyName, id);
                Server.lobbyMap.put(id,lobby);
                //
                Server.lobbyList.add(lobby);
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(messageCharset,clientChannel, true, lobby, " created by client."));
                if(playerAddress != null) Main.logr.log(Level.INFO, "LOBBY " + lobby.getLobby_id() + " CREATED BY CLIENT " + playerAddress.toString());
            } else {
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(messageCharset,clientChannel, false, null, "--> Lobby already exists"));
            }
        }
        else if((action == joinLobby || action == leaveLobby) && player != null){
            Lobby currentLobby = null;

            if(action == joinLobby){
                lobbyName = messageCharset.decode(lobbyBuffer).toString();
                currentLobby = Server.getLobbyByName(lobbyName);
                boolean checkResponse = (currentLobby != null);
                if(checkResponse) checkResponse = currentLobby.addPlayer(player);
                byte lobbyUsers = 0;
                if (checkResponse)
                    lobbyUsers = currentLobby.getUsersNumber();
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(messageCharset,clientChannel, checkResponse, currentLobby, " --> you are in.," + lobbyUsers));
                if(checkResponse) Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " JOINED LOBBY " + currentLobby.getLobby_id());

            } else if(action == leaveLobby){
                lobbyID = Integer.parseInt(messageCharset.decode(lobbyBuffer).toString());
                currentLobby = Server.lobbyMap.get(lobbyID);
                boolean checkResponse = (currentLobby != null);
                if(checkResponse) {
                    currentLobby.removePlayer(player);
                    if(player.isAdmin()) {
                        player.disableAdmin();
                        responseAction = becameAdmin + 10 ; sendResponseToClient(messageCharset, clientChannel, "You are no more admin.");
                        if(currentLobby.getAdmin() != null){
                        responseAction = becameAdmin; sendResponseToClient(messageCharset, currentLobby.getAdmin().getPlayerChannel(), "You are now admin.");}
                    }

                    Main.logr.log(Level.INFO, "CLIENT " + playerAddress.toString() + " LEFT LOBBY " + currentLobby.getLobby_id());
                }
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(messageCharset,clientChannel, checkResponse, currentLobby, " you are out."));
                if(checkResponse && currentLobby.isEmpty()) {
                    Server.lobbyMap.remove(currentLobby.getLobby_id());
                    Server.lobbyList.remove(currentLobby);
                }
            }

        }
        lobbyBuffer.clear();
    }

    private String getAllLobbyNames() {
        String temp = "";
        String lobbyNames = "";
        for (Lobby l : Server.lobbyList) {
            temp += (l.getLobbyName() + ",");
        }
        if (temp.length() > 0) {
            lobbyNames = temp.substring(0, temp.length() - 1);
        }
        return lobbyNames;
    }

    public void updateLobbyNameList() {
        String lobbyNames = getAllLobbyNames();

        if (lobbyNames == null)
            return;

        for (Player p : Server.playersLoggedin) {
            System.out.println("send...");
            sendToAllClients(p.getPlayerChannel(), lobbyNames);
        }

    }

    public void updateLobbyIDList() {
        String IDs = getAllLobbyIds(Server.lobbyMap);

        if (IDs == null)
            return;

        if (IDs.length() > 0 && IDs.charAt(0) == ',')
            IDs = IDs.substring(1);

        for (Player p : Server.playersLoggedin) {
            System.out.println("send...");
            sendToAllClients(p.getPlayerChannel(), IDs);
        }
    }

    private void sendToAllClients(SocketChannel clientChannel, String IDs) {
        System.out.println("Test: " + IDs);
        short actionResponse = 20;

        short dataLength = (short) IDs.length();
        ByteBuffer messageBuffer = ByteBuffer.allocate(6 + dataLength);

        messageBuffer.put(convertShortToByte(protocolName));
        messageBuffer.put(convertShortToByte(actionResponse));
        messageBuffer.put(convertShortToByte(dataLength));
        messageBuffer.put(IDs.getBytes(StandardCharsets.US_ASCII));
        messageBuffer.flip();

        try {
            clientChannel.write(messageBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageBuffer.clear();
    }



    private void parseBufferForMusicJoiner(Charset messageCharset, SocketChannel clientChannel, int dataSize, SelectionKey key) throws IOException {

        ByteBuffer toneBuffer;
        Player sender = null;

        toneBuffer = ByteBuffer.allocate(1);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        byte toneAction = messageCharset.decode(toneBuffer).toString().getBytes()[0];
        toneBuffer.clear();

        /*clientChannel.read(toneBuffer);
        toneBuffer.flip();
        byte toneType = messageCharset.decode(toneBuffer).toString().getBytes()[0];
        toneBuffer.clear();*/

        toneBuffer = ByteBuffer.allocate(dataSize - 1);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        toneData= messageCharset.decode(toneBuffer).toString();
        toneBuffer.clear();

        sender = (Player) key.attachment();
        if ( sender != null) {
            if(!sender.isMuted()) {
                Lobby clientLobby = Server.lobbyMap.get(sender.getLobbyId());
                responseAction = action;

                int i;
                if ((i = MusicJoiner.handleToneData(messageCharset, clientLobby, toneAction, toneData, responseAction, sender)) != 0) {
                    // for testing
                    if (i == -1)
                        System.out.println("Fehler in MusicJoiner -1");
                    else if (i == -2)
                        System.out.println("Fehler in MusicJoiner -2");
                    else
                        System.out.println("Fehler in MusicJoiner -??");
                }
            } else {
                MusicJoiner.sendTonToClient(messageCharset,sender.getPlayerChannel(),toneData + "," + sender.getId() + "," + toneAction, action);
            }

        } else {
            responseAction = (short) (action + 10);
            sendResponseToClient(messageCharset,clientChannel, "Error, client is DISCONNECTED");
        }
    }

    public void handleAction(Charset messageCharset, SocketChannel clientChannel, int bufferSize, SelectionKey key) throws IOException {
        if(bufferSize != 0){
            playerAddress = clientChannel.getRemoteAddress();
            if(action == login || action == logout || action == register){
                parseBufferForLoginSystem(messageCharset, clientChannel, key);
                updateLobbyNameList();
                testCorrect = true;
            }
            else if(action == createLobby || action == joinLobby || action == leaveLobby){
                parseBufferForLobbyOrGame(messageCharset, clientChannel, bufferSize, key);
                updateLobbyNameList();
                testCorrect = true;
            }
            else if(action == tone){
                parseBufferForMusicJoiner(messageCharset, clientChannel, bufferSize, key);
                testCorrect = true;
            }
            else if (action == passwordForgotten) {
                parseBufferIfPasswordForgotten(messageCharset, clientChannel, key);
                testCorrect = true;
            }
            else if (action == mutePlayer){
                parseBufferIfMutePlayer(messageCharset, clientChannel, bufferSize, key);
                testCorrect = true;
            }
        }
    }

    private void parseBufferIfMutePlayer(Charset messageCharset, SocketChannel clientChannel, int bufferSize, SelectionKey key) throws IOException {
        Player player = (Player) key.attachment();

        ByteBuffer bufferForName = ByteBuffer.allocate(bufferSize);
        clientChannel.read(bufferForName);
        bufferForName.flip();
        username = messageCharset.decode(bufferForName).toString();
        bufferForName.clear();

        if (player.getLobbyId() != -1) {
            Lobby lobby = Server.lobbyMap.get(player.getLobbyId());
            lobby.toggleMutePlayerByUsername(username);
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

    private String getLoginSystemResponse(int action, boolean result, SelectionKey key){
        int index = result? 0:1;
        this.responseAction = (short) action;
        //String IDs = "";
        String playerName = "";
        String playerID = "";
        if(action == 1){
            //IDs = getAllLobbyIds(Server.lobbyMap);
            Player player = (Player) key.attachment();
            playerName = "," + player.getName();
            playerID = "," + player.getId();
        }


        if(action == 8 || action == 18)
            return responsesArray[3][index];
        return responsesArray[action <= 10? action-1:action-11][index] + playerName + playerID;
    }

    private String getLobbyResponse(Charset messageCharset, SocketChannel clientChannel, boolean result, Lobby lobby, String additionPart){
        String message;
        if(result){
            responseAction = 21;
            if((action == 5 || action == 6) && !lobby.isEmpty()) {
                for (Player player : lobby.getPlayers()) {
                    System.out.println("send user names.");
                    sendResponseToClient(messageCharset, player.getPlayerChannel(), lobby.getPlayersListAsString());
                }
            }
            else{
                System.out.println("send user names.");
                sendResponseToClient(messageCharset, clientChannel, lobby.getPlayersListAsString());
            }
            message = "Lobby "+ lobby.getLobbyName() + "-" + lobby.getLobby_id() + additionPart;
            responseAction = action;

        }
        else{
            message = "either lobby is full or lobbyId is wrong or you are already in.";
            responseAction = (short) (action + 10);
        }
        return message;
    }

    private String getJoinResponse(boolean join, int id) {
        int index = join? 0:1;
        this.responseAction = join? (short) 9:19;
        return responsesArray[3][index] + Integer.toString(id);
    }

    public void sendResponseToClient(Charset messageCharset, SocketChannel clientChannel, String message){
        System.out.println("Test: " + message);
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


    public void resetProtocol() {
        playerAddress = null;

        action = 0;
        responseAction = 0;

        userNameSize = 0;
        emailSize = 0;
        passwordSize = 0;
    }


//-------------------------------------------------For Tests -------------------------------------------------------
    public void setAction(short a){action = a;}
    public void setPlayer(Player p) {player = p;}

    public void getParseLogin(Charset m, SocketChannel c, SelectionKey k) throws IOException {parseBufferForLoginSystem(m, c, k);}
    public void getParseLobby(Charset m, SocketChannel c, SelectionKey k) throws IOException {parseBufferForLobbyOrGame(m, c, 7, k);}
    public void getParseMusicJoiner(Charset m, SocketChannel c, SelectionKey k) throws IOException {parseBufferForMusicJoiner(m, c, 14, k);}
    public void getSize(Charset c, SocketChannel s) throws IOException {readSizes(c, s);}

    public String getEmail(){return email;}
    public String getUsername(){return username;}
    public String getPassword(){return password;}
    public String getLobbyName(){return lobbyName;}
    public byte getEmailSize(){return emailSize;}
    public byte getUserNameSize(){return userNameSize;}
    public byte getPasswordSize(){return passwordSize;}
    public String getToneData(){return toneData;}
    public boolean test(){return testCorrect;}

    public String getLobbyID(){return lobbyName;}
}
