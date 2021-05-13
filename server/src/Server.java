package src;

import src.exceptions.IPAddressException;
import src.exceptions.SocketBindException;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.sql.SQLException;
import java.util.*;

import static java.lang.System.exit;

public class Server {
    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private static CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;

    private static boolean running = true;
    private static boolean finished = false;

    private static int noOfLobbies = 0;
    private static int playersId = 0;

    public static HashMap<Integer,Lobby> lobbyMap = new HashMap<>();
    public static HashMap<Integer,Player> loggedInPlayers = new HashMap<>();


    public void setupServerAddress(String address, int port) throws IPAddressException {
        try {
            serverAddress = new InetSocketAddress(address, port);
        } catch (IllegalArgumentException | SecurityException e) {
            throw new IPAddressException();
        }
    }

    //TODO IllegalArgumentException
    public void defineCharType() {
        messageCharset = Charset.forName("US-ASCII");

        decoder = messageCharset.newDecoder();
        encoder = messageCharset.newEncoder();
    }

    public void OpenSelectorAndSetupSocket() throws IOException, SocketBindException {

        selector = Selector.open();

        serverChannel = ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        try {
            serverChannel.socket().bind(serverAddress);
        } catch (IOException e) {
            throw new SocketBindException();
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

        while (running) {
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

        finished = true;
    }

    public static int createLobbyId(){
        return noOfLobbies += 1;
    }

    public static int createPlayerId(){
        return playersId += 1;
    }

    public void finishServer() {
        running = false;
        if (selector != null)
            selector.wakeup();
        while (!finished) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 3; i > 0; i--) {
            try {
                if (serverChannel != null)
                    serverChannel.close();
                if (selector != null)
                    selector.close();
                i = 0;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void setFinishedTrue() {
        finished = true;
    }

}
