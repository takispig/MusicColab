package com.example.musiccolab;

import com.example.musiccolab.exceptions.IPAddressException;
import com.example.musiccolab.exceptions.SocketBindException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ServerTest {
    private Charset messageCharset = null;
    private CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;

    public static String result = "";
    public static short protocolName = 0;

    private final Protocol protocol = new Protocol();

    private boolean running = true;
    private boolean finished = false;

    private static int noOfLobbies = -1;
    private static int noOfPlayers = -1;

    public static HashMap<Integer, Lobby> lobbyMap = new HashMap<>();
    public static HashMap<Integer, Player> loggedInPlayers = new HashMap<>();
    public static HashMap<SocketChannel, Player> loggedInList = new HashMap<>();

    Server server = new Server();

    @Test
    void protocolFromServer(){
        assert server.getProtocol() != null;
    }

    @Test
    void setupServerCorrectAddress() throws IPAddressException {
        server.setupServerAddress("localhost", 1200);
        assertNotNull(server.getServerAddressForTesting());

    }

    @Test
    void setupServerIncorrectAddress() {
        try {
            server.setupServerAddress("-1", -2);
        }catch (IPAddressException e) {
            assertNull(server.getServerAddressForTesting());
        }

    }

    @Test
    void defineCharTypeTest() throws IPAddressException {
        server.setupServerAddress("localhost", 1200);
        server.defineCharType();
        assertNotNull(server.getMessageCharsetForTesting());
        assertNotNull(server.getDecoderForTesting());
        assertNotNull(server.getEncoderForTesting());
    }

    @Test
    void openSelectorAndSetupSocketCorrect() throws IPAddressException, IOException, SocketBindException {
        server.setupServerAddress("localhost", 1200);
        server.defineCharType();
        server.OpenSelectorAndSetupSocket();
        assertNotNull(server.getSelectorForTesting());
        assertNotNull(server.getServerChannelForTesting());
    }

    @Test
    void openSelectorAndSetupSocketIncorrect() throws IPAddressException, IOException, SocketBindException {
        server.setupServerAddress("local", 1200);
        server.defineCharType();
        try {
            server.OpenSelectorAndSetupSocket();
        }catch (SocketBindException e){
            assertNull(server.getServerChannelForTesting().getLocalAddress());
        }
        assertNotNull(server.getSelectorForTesting());
        assertNotNull(server.getServerChannelForTesting());
    }

    @Test
    void handleConnectionWhenAcceptable() throws IPAddressException, IOException, SocketBindException {

        resetProperties();

        setupServerAddress("localhost", 1201);
        defineCharType();
        OpenSelectorAndSetupSocket();
        server.setSelector(selector);
        server.setMessageCharset(messageCharset);

        int action = 3;
        Client thread = new Client(1, Thread.currentThread(), (short)action);
        thread.start();

        selector.select();

        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

        while (selectedKeys.hasNext()) {
            SelectionKey key = (SelectionKey) selectedKeys.next();

            if (key.isAcceptable()) {
                server.acceptForTest(key);

            }
            selectedKeys.remove();
        }
        try {
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
                Thread.currentThread().wait(2000);
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        System.out.println(result);
        assert result.equals("Welcome in MusicCoLab Server.\r\n");
        thread.client.communicationThread.stop();
        thread.thread.stop();
    }

    @Test
    void handleConnectionWhenReadableWrongProtocolName() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1202);
        defineCharType();
        OpenSelectorAndSetupSocket();
        server.setSelector(selector);
        server.setMessageCharset(messageCharset);

        int action = 3;
        protocolName = 12844;
        Client thread = new Client(2, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    server.acceptForTest(key);
                    counter = 1;
                } else if (key.isReadable()) {
                    server.handleReadableForTest(key);
                    counter = 2;
                }
                selectedKeys.remove();
            }
        }
        try {
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        System.out.println(result);
        assert result.substring(result.indexOf("Y")).equals("You are not our customer.\r\n");
    }

    @Test
    void handleConnectionWhenReadableWrongAction() throws IPAddressException, IOException, SocketBindException {
        resetProperties();

        setupServerAddress("localhost", 1206);
        defineCharType();
        OpenSelectorAndSetupSocket();
        server.setSelector(selector);
        server.setMessageCharset(messageCharset);

        int action = 50;
        protocolName = 12845;
        Client thread = new Client(6, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    server.acceptForTest(key);
                    counter = 1;
                } else if (key.isReadable()) {
                    server.handleReadableForTest(key);
                    counter = 2;
                }
                selectedKeys.remove();
            }
        }
        try {
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        System.out.println(result);
        assert result.substring(result.indexOf("A")).equals("Action is not known.\r\n");
    }

    @Test
    void handleConnectionWhenReadableDisconnectClient() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        SelectionKey key = null;

        setupServerAddress("localhost", 1207);
        defineCharType();
        OpenSelectorAndSetupSocket();
        server.setSelector(selector);
        server.setMessageCharset(messageCharset);

        int action = 3;
        protocolName = 12845;
        Client thread = new Client(7, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    server.acceptForTest(key);
                    counter = 1;
                } else if (key.isReadable()) {
                    Thread.sleep(1000);
                    server.handleReadableForTest(key);
                    counter = 2;
                }
                selectedKeys.remove();
            }
        }
        System.out.println(result);
        assert !key.channel().isOpen();
    }

    @Test
    void handleConnection() throws IPAddressException, IOException, SocketBindException {

        resetProperties();
        SelectionKey key = null;

        setupServerAddress("localhost", 1208);
        defineCharType();
        OpenSelectorAndSetupSocket();
        server.setSelector(selector);
        server.setMessageCharset(messageCharset);

        int action = 3;
        protocolName = 12845;
        Client thread = new Client(8, Thread.currentThread(), (short)action);
        thread.start();

        CommunicationHandling.server = server;
        server.handleConnection();

        assert(!server.isRunningForTesting());
        assert(server.isFinishedForTesting());
    }

    @Test
    void finishServer() throws IPAddressException, IOException, SocketBindException {

        resetProperties();
        SelectionKey key = null;

        setupServerAddress("localhost", 1209);
        defineCharType();
        OpenSelectorAndSetupSocket();
        server.setSelector(selector);
        server.setMessageCharset(messageCharset);

        int action = 3;
        protocolName = 12845;
        Client thread = new Client(9, Thread.currentThread(), (short)action);
        thread.start();


        CommunicationHandling.server = server;
        server.handleConnection();
        server.finishServer();

        assert !server.isRunningForTesting();
        assert server.getServerChannelForTesting() == null;
        assert !server.getSelectorForTesting().isOpen();
    }

    @Test
    void createLobbyId() {
        List<Integer> Ids = new LinkedList<>();
        for(byte index = 0; index < 10; index++)
            Ids.add(Server.createLobbyId());
        assert(!Ids.isEmpty());
        for(byte index = 0; index < 10; index++)
            assert(Ids.get(index) >= 0);
    }

    @Test
    void createPlayerId() {
        List<Integer> Ids = new LinkedList<>();
        for(byte index = 0; index < 10; index++)
            Ids.add(Server.createPlayerId());
        assert(!Ids.isEmpty());
        for(byte index = 0; index < 10; index++)
            assert(Ids.get(index) >= 0);
    }

    @Test
    void setFinishTrue(){
        server.setFinishedTrue();
        assert server.isFinishedForTesting();
    }

    @Test
    void getLobbyNameNull(){
        assertNull(Server.getLobbyByName("lobby"));
    }

    @Test
    void getLobbyName() throws IOException {
        Server.lobbyList.add(new Lobby(new Player("player", "1", "1", 1, null), "lobby", 0));
        assertNotNull(Server.getLobbyByName("lobby"));
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

    private void resetProperties(){
        messageCharset = null;
        decoder = null;//Network order = Byte --> Characters = Host order
        encoder = null;//Characters = Host order -->  Network order = Byte
        serverChannel = null;
        serverAddress = null;
        selector = null;
        result = "";
    }
}