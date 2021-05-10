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
    final private int loginError = 11;

    final private int logout = 2;
    final private int logoutError = 12;

    final private int register = 3;
    final private int registerError = 13;

    final private int createLobby = 4;
    final private int createLobbyError = 14;

    final private int joinLobby = 5;
    final private int joinLobbyError = 15;

    final private int leaveLobby = 6;
    final private int leaveLobbyError = 15;

    final private int tone = 7;
    final private int toneError = 17;

    final private int gameStart = 8;
    final private int GameStartError = 18;

    final private int gameEnd = 9;
    final private int gameEndError = 19;

    final private int gameRestart = 10;
    final private int gameRestartError = 20;

    private short action;
    private short responseAction;

    private byte userNameSize;
    private byte emailSize;
    private byte passwordSize;



    final private String[][] responsesArray = { {"Client logged in", "error"},
                                          {"Client logged out", "error"},
                                          {"Client registered", "error"},};

    public Protocol(){
        for(short index = 1; index < 11; index++)
            codesList.add(index);
    }


    /**
     * @return: -1 if protocol name is wrong.
     * -2 if Action is not known.
     * else size of Data.
     */
    public short[] analyseMainBuffer(Charset messageCharset, SocketChannel clientChannel) throws IOException {
        short[] temp;

        ByteBuffer mainBuffer = ByteBuffer.allocate(2);
        clientChannel.read(mainBuffer);
        mainBuffer.flip();

        if (protocolName == getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))) {
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
            return new short[]{action, -1};
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

    private void parseBufferForLoginSystem(Charset messageCharset, SocketChannel clientChannel) throws IOException, SQLException, ClassNotFoundException {

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
            checkResponse = LoginSystem.register(username, email, password);
            sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse? action:action+10, checkResponse));
        }
        else if(action == login) {// TODO: LoginSystem.getId(username, password), Just registered users can login.
            checkResponse = LoginSystem.login(username, password, clientChannel);
            sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse? action:action+10, checkResponse));
        }
        else{// TODO: LoginSystem.getId(username, password), Just logged in users can logout.
            checkResponse = LoginSystem.logout(username, password);
            sendResponseToClient(messageCharset, clientChannel, getLoginSystemResponse(checkResponse? action:action+10, checkResponse));
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
            int id = Communication.createLobbyId();
            Lobby lobby = new Lobby(player, lobbyName, id);
            Communication.lobbyMap.put(id,lobby); //lobby.getLobby_id()
            sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(true, lobby));
        }
        else if((action == joinLobby || action == leaveLobby) && player != null){
            int lobbyID = Integer.parseInt(messageCharset.decode(lobbyBuffer).toString());
            Lobby currentLobby = Communication.lobbyMap.get(lobbyID);
            if(action == joinLobby && currentLobby != null){
                boolean checkResponse = currentLobby.addPlayer(player);
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(checkResponse, currentLobby));
            } else if(action == leaveLobby && currentLobby != null){
                currentLobby.removePlayer(player);
                sendResponseToClient(messageCharset,clientChannel,getLobbyResponse(true, currentLobby));
            }

        }
        else if(player != null){
            int lobbyID = Integer.parseInt(messageCharset.decode(lobbyBuffer).toString());
            Game game = new Game(Communication.lobbyMap.get(lobbyID));
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

        musicJoiner = new MusicJoiner(toneAction, toneType, toneData);
    }

    public void handleAction(Charset messageCharset, SocketChannel clientChannel, int bufferSize) throws IOException, SQLException, ClassNotFoundException {
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
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }

    private String getLoginSystemResponse(int action, boolean result){
        int index = result? 1:0;
        this.responseAction = (short) action;
        return responsesArray[action <= 10? action-1:action-11][index];
    }

    private String getLobbyResponse(boolean result, Lobby lobby){
        String message;
        if(result){
            message = "Lobby "+ lobby.getLobby_id()+" created by Client";
            responseAction = action;
        }
        else{
            message = "Error";
            responseAction = (short) (action + 10);
        }
        return message;
    }

    public void sendResponseToClient(Charset messageCharset, SocketChannel clientChannel, String message) throws IOException {
        short dataLength = (short) message.length();
        ByteBuffer messageBuffer = ByteBuffer.allocate(6 + dataLength);
        messageBuffer.put(convertShortToByte(protocolName));
        messageBuffer.put(convertShortToByte(responseAction));
        messageBuffer.put(convertShortToByte(dataLength));
        messageBuffer.put(message.getBytes(messageCharset));
        messageBuffer.flip();
        clientChannel.write(messageBuffer);
        messageBuffer.clear();
        if(action == register || action == logout)
            clientChannel.close();
    }
}
