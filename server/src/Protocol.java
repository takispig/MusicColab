import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Protocol {
    final private String protocolName = "MusicLoginVersion1.0";
    final private int tone = 212;
    final private List<Integer> codeList = new LinkedList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

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

    final private int gameStart = 7;
    final private int GameStartError = 71;

    final private int gameEnd = 8;
    final private int gameEndError = 81;

    final private int gameRestart = 9;
    final private int gameRestartError = 91;

    private int action;

    private String username ;
    private int userNameSize;
    private String email;
    private int emailSize;
    private String password;
    private int passwordSize;

    private String lobbyID;
    private String lobbyName;

    private String toneData;
    private String toneType;
    private int toneAction;



    /**
     * @param mainBuffer: first 6 Bytes from client message, but as ByteBuffer.
     * @return: -1 if protocol name is wrong.
     * -2 if Action is not known.
     * else size of Data.
     */
    public int analyseMainBuffer(Charset messageCharset, SocketChannel clientChannel) throws IOException {
        ByteBuffer mainBuffer = ByteBuffer.allocate(2);
        clientChannel.read(mainBuffer);
        mainBuffer.flip();

        if (protocolName.equals(messageCharset.decode(mainBuffer).toString())) {
            mainBuffer.clear();

            clientChannel.read(mainBuffer);
            mainBuffer.flip();
            action = Integer.parseInt(messageCharset.decode(mainBuffer).toString());
            if (codeList.contains(action)) {
                mainBuffer.clear();

                clientChannel.read(mainBuffer);
                mainBuffer.flip();
                return Integer.parseInt(messageCharset.decode(mainBuffer).toString());

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

    private void readSizes(Charset messageCharset, SocketChannel clientChannel) throws IOException {

        ByteBuffer loginSystemBuffer = ByteBuffer.allocate(1);
        if(action == register){
            clientChannel.read(loginSystemBuffer);
            loginSystemBuffer.flip();
            emailSize = Integer.parseInt(messageCharset.decode(loginSystemBuffer).toString());
            loginSystemBuffer.clear();
        }

        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        userNameSize = Integer.parseInt(messageCharset.decode(loginSystemBuffer).toString());
        loginSystemBuffer.clear();

        clientChannel.read(loginSystemBuffer);
        loginSystemBuffer.flip();
        passwordSize = Integer.parseInt(messageCharset.decode(loginSystemBuffer).toString());
        loginSystemBuffer.clear();
    }

    private void parseBufferForLoginSystem(Charset messageCharset, SocketChannel clientChannel) throws IOException {

        readSizes(messageCharset, clientChannel);
        ByteBuffer loginSystemBuffer;
        LoginSystem loginSystem;

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
            loginSystem = new LoginSystem(username, password, email);
        else
            loginSystem = new LoginSystem(username, password, "");
    }

    private void parseBufferForLobbyOrGame(Charset messageCharset, SocketChannel clientChannel, int lobbyNameIsSize) throws IOException {

        ByteBuffer lobbyBuffer;
        Lobby lobby;
        Game game;

        lobbyBuffer = ByteBuffer.allocate(lobbyNameIsSize);
        clientChannel.read(lobbyBuffer);
        lobbyBuffer.flip();
        if(action == createLobby){
            lobbyName = messageCharset.decode(lobbyBuffer).toString();
            lobby = new Lobby(lobbyName);
        }
        else if(action == joinLobby || action == leaveLobby){
            lobbyID = messageCharset.decode(lobbyBuffer).toString();
            lobby = new Lobby(lobbyID);
        }
        else{
            lobbyID = messageCharset.decode(lobbyBuffer).toString();
            game = new Game(lobbyID, action);
        }
        lobbyBuffer.clear();
    }

    private void parseBufferForMusicJoiner(Charset messageCharset, SocketChannel clientChannel, int dataSize) throws IOException {

        ByteBuffer toneBuffer;
        MusicJoiner musicJoiner;

        toneBuffer = ByteBuffer.allocate(1);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        toneAction = Integer.parseInt(messageCharset.decode(toneBuffer).toString());
        toneBuffer.clear();

        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        toneType = messageCharset.decode(toneBuffer).toString();
        toneBuffer.clear();

        toneBuffer = ByteBuffer.allocate(dataSize - 2);
        clientChannel.read(toneBuffer);
        toneBuffer.flip();
        toneData= messageCharset.decode(toneBuffer).toString();
        toneBuffer.clear();

        musicJoiner = new MusicJoiner(toneAction, toneType, toneData);
    }

    public void handleAction(Charset messageCharset, SocketChannel clientChannel, int bufferSize) throws IOException {
        if(bufferSize != 0){
            if(action == login || action == logout || action == register){
                parseBufferForLoginSystem(messageCharset, clientChannel);
            }
            else if(action == createLobby || action == joinLobby || action == leaveLobby){
                parseBufferForLobbyOrGame(messageCharset, clientChannel, bufferSize);
            }
            else if(action == tone){
                parseBufferForMusicJoiner(messageCharset, clientChannel, bufferSize);
            }
        }
    }
}
