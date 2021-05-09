package src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class Protocol {
    final private short protocolName = 12845;
    int playerId;
    InetSocketAddress playerAddress;
    final private List<Short> codeList = new ArrayList<Short>();

    final private int login = 1;
    final private int loginError = 11;

    final private int logout = 2;
    final private int logoutError = 21;

    final private int register = 3;
    final private int registerError = 31;

    final private int createLobby = 4;
    final private int createLobbyError = 41;

    final private int joinLobby = 5;
    final private int joinLobbyError = 51;

    final private int leaveLobby = 6;
    final private int leaveLobbyError = 61;

    final private int tone = 7;
    final private int toneError = 71;

    final private int gameStart = 8;
    final private int GameStartError = 81;

    final private int gameEnd = 9;
    final private int gameEndError = 91;

    final private int gameRestart = 10;
    final private int gameRestartError = 101;

    private short action;

    private String username ;
    private byte userNameSize;
    private String email;
    private byte emailSize;
    private String password;
    private byte passwordSize;

    private List<String> LobbyIdList = new LinkedList<>();
    private int lobbyID;
    private String lobbyName;

    private String toneData;
    private byte toneType;
    private byte toneAction;

    private HashMap<Integer,Lobby> lobbyMap = new HashMap<>();

    public Protocol(){
        for(short index = 1; index < 11; index++)
            codeList.add(index);
    }


    /**
     * @param b: first 6 Bytes from client message, but as ByteBuffer.
     * @return: -1 if protocol name is wrong.
     * -2 if Action is not known.
     * else size of Data.
     */
    private static short getShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }

    private String getLobbyId(){
        Random rand = new Random();
        int id = rand.nextInt(Integer.MAX_VALUE);
        while (LobbyIdList.contains(id))
            id = rand.nextInt(Integer.MAX_VALUE);
        return Integer.toString(id);
    }

    public short analyseMainBuffer(Charset messageCharset, SocketChannel clientChannel) throws IOException {
        ByteBuffer mainBuffer = ByteBuffer.allocate(2);
        clientChannel.read(mainBuffer);
        mainBuffer.flip();

        if (protocolName == getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))) {
            mainBuffer.clear();

            clientChannel.read(mainBuffer);
            mainBuffer.flip();
            action = getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));
            if (codeList.contains(action)) {
                mainBuffer.clear();

                clientChannel.read(mainBuffer);
                mainBuffer.flip();
                return getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));

            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    public void SendErrorToClient(Charset messageCharset, SocketChannel clientChannel, String errorMessage) throws IOException {
        ByteBuffer errorBuffer = ByteBuffer.allocate(100);
        errorBuffer.put(errorMessage.getBytes(messageCharset));
        errorBuffer.flip();
        clientChannel.write(errorBuffer);
        errorBuffer.clear();
    }

    public void sendResponseToClient(Charset messageCharset, SocketChannel clientChannel, String message) throws IOException {
        ByteBuffer messageBuffer = ByteBuffer.allocate(100);
        messageBuffer.put(message.getBytes(messageCharset));
        messageBuffer.flip();
        clientChannel.write(messageBuffer);
        messageBuffer.clear();
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

    private void parseBufferForLoginSystem(Charset messageCharset, SocketChannel clientChannel) throws IOException, SQLException, ClassNotFoundException {

        readSizes(messageCharset, clientChannel);
        ByteBuffer loginSystemBuffer;

        if(action == register){
            loginSystemBuffer = ByteBuffer.allocate(emailSize);
            clientChannel.read(loginSystemBuffer);
            loginSystemBuffer.flip();
            username = messageCharset.decode(loginSystemBuffer).toString();
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


        if(action == register)
            if(LoginSystem.register(username, email, password)){
                //TODO: Official protocol answer: registered
                sendResponseToClient(messageCharset,clientChannel,"Client registered");
            }
        else if(action == login)
            if(LoginSystem.login(username, password,clientChannel)){
                //TODO: Official Protocol-Response: login
                sendResponseToClient(messageCharset,clientChannel,"Client logged in");
            }
        else
            if(LoginSystem.logout(username, password)){
                //TODO: Official Protocol-Response: logout
                sendResponseToClient(messageCharset,clientChannel,"Client logged out");
            }
    }

    private void parseBufferForLobbyOrGame(Charset messageCharset, SocketChannel clientChannel, int lobbyNameIsSize) throws IOException {

        ByteBuffer lobbyBuffer;
        Lobby lobby = null;
        Game game;

        lobbyBuffer = ByteBuffer.allocate(lobbyNameIsSize);
        clientChannel.read(lobbyBuffer);
        lobbyBuffer.flip();
        if(action == createLobby){
            lobbyName = messageCharset.decode(lobbyBuffer).toString();
            Player player = LoginSystem.getPlayerByChannel(clientChannel);
            lobby = new Lobby(player);
            lobbyMap.put(lobby.getLobby_id(),lobby);
            //TODO: Official Protocol-Response: lobby-create
            sendResponseToClient(messageCharset,clientChannel,"Lobby "+ lobby.getLobby_id()+" created by Client");
        }
        else if(action == joinLobby || action == leaveLobby){
            lobbyID = Integer.valueOf(messageCharset.decode(lobbyBuffer).toString());//function to add player to a lobby is needed.
            Player player = LoginSystem.getPlayerByChannel(clientChannel);
            Lobby lobby1 = lobbyMap.get(lobbyID);
            if(action == joinLobby && lobby1 != null){
                lobby1.addPlayer(player);
                //TODO: Official Protocol-Response: Lobby-join
                sendResponseToClient(messageCharset,clientChannel,"Client joined the Lobby: "+ lobbyID);
            } else if(action == leaveLobby && lobby1 != null){
                lobby1.removePlayer(player);
                //TODO: Official Protocol-Response: Lobby-leave
                sendResponseToClient(messageCharset,clientChannel,"Client left the lobby: "+ lobbyID);
            } else throw new RuntimeException("Lobby not found"); //TODO: Error handling

        }
        else{
            lobbyID = Integer.valueOf(messageCharset.decode(lobbyBuffer).toString());
            //TODO: Create new Game
            game = new Game(lobbyMap.get(lobbyID));
            //TODO: Official Protocol-Response: Game start
            sendResponseToClient(messageCharset,clientChannel,"Game started");
        }
        lobbyBuffer.clear();
    }



    private void parseBufferForMusicJoiner(Charset messageCharset, SocketChannel clientChannel, int dataSize) throws IOException {

        ByteBuffer toneBuffer;
        MusicJoiner musicJoiner;

        toneBuffer = ByteBuffer.allocate(1);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        toneAction = messageCharset.decode(toneBuffer).toString().getBytes()[0];
        toneBuffer.clear();

        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        toneType = messageCharset.decode(toneBuffer).toString().getBytes()[0];
        toneBuffer.clear();

        toneBuffer = ByteBuffer.allocate(dataSize - 2);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        toneData= messageCharset.decode(toneBuffer).toString();
        toneBuffer.clear();
        //TODO: MusikJoiner
        //musicJoiner = new MusicJoiner(toneAction, toneType, toneData);
    }

    public void handleAction(Charset messageCharset, SocketChannel clientChannel, int bufferSize, int id) throws IOException, SQLException, ClassNotFoundException {
        if(bufferSize != 0){
            playerId = id;
            playerAddress = (InetSocketAddress) clientChannel.getRemoteAddress();

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
}
