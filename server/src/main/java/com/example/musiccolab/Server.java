package main.java.com.example.musiccolab;

import main.java.com.example.musiccolab.exceptions.IPAddressException;
import main.java.com.example.musiccolab.exceptions.SocketBindException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.*;
import java.sql.SQLException;
import java.util.*;

import static java.lang.System.exit;

public class Server {
    private Charset messageCharset = null;
    private CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;

    private boolean running = true;
    private boolean finished = false;

    private static int noOfLobbies = -1;
    private static int noOfPlayers = -1;

    public static HashMap<Integer,Lobby> lobbyMap = new HashMap<>();
    public static HashMap<Integer,Player> loggedInPlayers = new HashMap<>();

    private static Protocol protocol = new Protocol();

    public void setupServerAddress(String address, int port) throws IPAddressException {
        try {
            serverAddress = new InetSocketAddress(address, port);
        } catch (IllegalArgumentException | SecurityException e) {
            throw new IPAddressException();
        }
    }

    public void defineCharType() {
        messageCharset = StandardCharsets.US_ASCII;

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


        String message = "Welcome in MusicCoLab Server.\r\n";
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
    private void handleConnectionWhenReadable(SelectionKey key) throws IOException {

        SocketChannel clientChannel = (SocketChannel) key.channel();
        //Read the first 6 indexes. (Protocol name, Action and data length. 2 Bytes each)
        Player disconnectedPlayer = (Player) key.attachment();


        short[] result = protocol.analyseMainBuffer(messageCharset, clientChannel);
        if(result[1] == -1) {
            protocol.sendResponseToClient(messageCharset, clientChannel, "You are not our customer.\r\n");
            if (disconnectedPlayer != null) {
                disconnectedPlayer.state.setState(ClientState.DISCONNECTED);
            }
            clientChannel.close();
        }
        else if(result[1] == -2) {
            if(result[0] != 0) {
                protocol.sendResponseToClient(messageCharset, clientChannel, "Action is not known.\r\n");
                if (disconnectedPlayer != null) {
                    disconnectedPlayer.state.setState(ClientState.DISCONNECTED);
                }
                clientChannel.close();
            }
        }
        else if(result[1] == -3) {
            System.out.println("Client is disconnected.");
            int id = -1;
            if(disconnectedPlayer != null){
                id = disconnectedPlayer.getLobbyId();
                loggedInPlayers.remove(disconnectedPlayer.getId());
            }
            Lobby lobbyOfDisconnectedPlayer = null;
            if(id != -1) {
                lobbyOfDisconnectedPlayer = lobbyMap.get(id);
                lobbyOfDisconnectedPlayer.removePlayer(disconnectedPlayer);
                if(lobbyOfDisconnectedPlayer.isEmpty()) {
                    lobbyMap.remove(id);
                }
                disconnectedPlayer.state.setState(ClientState.DISCONNECTED);
            }

            clientChannel.close();
        }
        else if(result[1] != 0)
            protocol.handleAction(messageCharset, clientChannel, result[1], key);

        protocol.resetProtocol();
    }

    public void handleConnection() throws IOException {
        System.out.println("Waiting for connection: ");

        while (running) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

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
        noOfLobbies++;
        return noOfLobbies;
    }
    public static int createPlayerId(){
        noOfPlayers++;
        return noOfPlayers;
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

//_____________________________________________________________________________________________________________________
//                                                 For testing                                                       //
//_____________________________________________________________________________________________________________________
    public InetSocketAddress getServerAddressForTesting(){
        return serverAddress;
    }

    public Selector getSelectorForTesting(){
        return selector;
    }
    public void setSelector(Selector s){selector = s;}

    public ServerSocketChannel getServerChannelForTesting(){
        return serverChannel;
    }

    public Charset getMessageCharsetForTesting(){
        return messageCharset;
    }
    public void setMessageCharset(Charset m){messageCharset = m;}

    public CharsetDecoder getDecoderForTesting(){
        return decoder;
    }

    public CharsetEncoder getEncoderForTesting(){
        return encoder;
    }

    public boolean isRunningForTesting(){
        return running;
    }

    public boolean isFinishedForTesting(){
        return finished;
    }

    public void acceptForTest(SelectionKey key) throws IOException { handleConnectionWhenAcceptable(key);}

    public void handleForTest(SelectionKey key) throws IOException { handleConnectionWhenReadable(key);}
}
