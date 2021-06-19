package test;

import main.java.com.example.musiccolab.Lobby;
import main.java.com.example.musiccolab.Player;
import main.java.com.example.musiccolab.Protocol;
import main.java.com.example.musiccolab.exceptions.IPAddressException;
import main.java.com.example.musiccolab.exceptions.SocketBindException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ProtocolTest {

    private Charset messageCharset = null;
    private CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;

    private final Protocol protocol = new Protocol();

    private boolean running = true;
    private boolean finished = false;

    private static int noOfLobbies = -1;
    private static int noOfPlayers = -1;

    public static HashMap<Integer, Lobby> lobbyMap = new HashMap<>();
    public static HashMap<Integer, Player> loggedInPlayers = new HashMap<>();
    public static HashMap<SocketChannel, Player> loggedInList = new HashMap<>();


    @Test
    void analyseMainBuffer() throws IPAddressException, IOException, SocketBindException {

        setupServerAddress("192.168.178.42", 1200);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 3;
        Client thread = new Client(0, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);

                } else if (key.isReadable()) {
                    short[] result = protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    action = result[0];
                    List<Integer> returnValue = new LinkedList<>(Arrays.asList(-3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
                    assert returnValue.contains(action);
                }
                selectedKeys.remove();
            }

            if(counter == 1) {
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error with waiting of main thread.");
                }
            }
            counter++;
        }
    }

    @Test
    void ParseForLoginSystem() throws IPAddressException, IOException, SocketBindException {
        resetProperties();

        setupServerAddress("192.168.178.42", 1203);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 3;
        Client thread = new Client(3, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);

                } else if (key.isReadable()) {
                    protocol.getParseLogin(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getEmail().equals("zead@gmail.com");
                    assert protocol.getUsername().equals("zead");
                    assert protocol.getPassword().equals("123");
                }
                selectedKeys.remove();
            }

            if(counter == 1) {
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error with waiting of main thread.");
                }
            }
            counter++;
        }
    }

    @Test
    void ParseForLobby() throws IPAddressException, IOException, SocketBindException {
        resetProperties();

        setupServerAddress("192.168.178.42", 1204);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 4;
        Client thread = new Client(4, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);

                } else if (key.isReadable()) {
                    protocol.getParseLobby(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getLobbyName().equals("example");
                }
                selectedKeys.remove();
            }

            if(counter == 1) {
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error with waiting of main thread.");
                }
            }
            counter++;
        }
    }

    @Test
    void readSizes() throws IPAddressException, IOException, SocketBindException {
        resetProperties();

        setupServerAddress("192.168.178.42", 1205);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 3;
        Client thread = new Client(5, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);

                } else if (key.isReadable()) {
                    protocol.getSize(messageCharset, (SocketChannel) key.channel());
                    assert protocol.getEmailSize() == 14;
                    assert protocol.getUserNameSize() == 4;
                    assert protocol.getPasswordSize() == 3;
                }
                selectedKeys.remove();
            }

            if(counter == 1) {
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error with waiting of main thread.");
                }
            }
            counter++;
        }
    }

    @Test
    void convertShortToByte() {
        short value = 55;
        byte[] returnValue = protocol.convertShortToByte(value);
        for(byte b : returnValue)
            assert b >= 0;
    }

    @Test
    void sendResponseToClient() {
    }

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

    private void resetProperties(){
        messageCharset = null;
        decoder = null;//Network order = Byte --> Characters = Host order
        encoder = null;//Characters = Host order -->  Network order = Byte
        serverChannel = null;
        serverAddress = null;
        selector = null;
    }
}