import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;
import java.util.*;

import static java.lang.System.exit;

public class Communication {
    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private static CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;

    private static int noOfLobbies = -1;
    private static int playersId = -1;

    public static HashMap<Integer,Lobby> lobbyMap = new HashMap<>();
    public static HashMap<Integer,Player> loggedInPlayers = new HashMap<>();

    private static void printUsage() {

        System.err.println("Usage: MusicCoLabServer needs <address> <port>");
    }

    public void CheckParameters(int length){
        //we need address and port, so we have two parameters.
        if(length != 2){
            printUsage();
            exit(1);
        }
    }

    public void defineCharType(String Address, String Port){
        try {
            messageCharset = Charset.forName("US-ASCII");
        } catch(UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            System.exit(1);
        }
        decoder = messageCharset.newDecoder();
        encoder = messageCharset.newEncoder();

        try {
            serverAddress = new InetSocketAddress(Address, Integer.parseInt(Port));
        } catch (IllegalArgumentException | SecurityException e) {
            printUsage();
            exit(1);
        }
    }

    public void OpenSelectorAndSetupSocket(){
        try {
            selector = Selector.open();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverChannel.socket().bind(serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }

    //bufferHandleIsNeeded?

    private void handleConnectionWhenAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel sChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = sChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE); //AddState as third parameter.


        String message = "Welcome in MusicCoLab Server.";
        ByteBuffer tempBuffer = ByteBuffer.allocate(message.length());
        tempBuffer.put(message.getBytes(messageCharset));
        tempBuffer.flip();
        channel.write(tempBuffer);
        tempBuffer.clear();
    }

    /**
     * According to the return value of function "analyseMainBuffer" send an error message or
     * handle the received action.
     */
    private void handleConnectionWhenReadable(SelectionKey key) throws IOException, SQLException, ClassNotFoundException {
        //int state = (Integer) key.attachment(); //To save the state of all clients. Integer --> Class

        SocketChannel clientChannel = (SocketChannel) key.channel();
        //Read the first 6 indexes. (Protocol name, Action and data length. 2 Bytes each)

        Protocol protocol = new Protocol();

        short[] result = protocol.analyseMainBuffer(messageCharset, clientChannel);
        if(result[1] == -1) {
            protocol.sendResponseToClient(messageCharset, clientChannel, "You are not our customer.");
            clientChannel.close();
        }
        else if(result[1] == -2) {
            protocol.sendResponseToClient(messageCharset, clientChannel, "Action is not known.");
            clientChannel.close();
        }
        else
            protocol.handleAction(messageCharset, clientChannel, result[1]);
    }

    public void handleConnection() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Waiting for connection: ");

        while (true) {
            selector.select();

            Iterator selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);

                } else if (key.isReadable()) {
                    handleConnectionWhenReadable(key);
                }
                selectedKeys.remove();
            }
        }
    }

    public static int createLobbyId(){
        return noOfLobbies += 1;
    }

    public static int createPlayerId(){
        return playersId += 1;
    }
}
