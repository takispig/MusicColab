package test;

import main.java.com.example.musiccolab.Lobby;
import main.java.com.example.musiccolab.Player;
import main.java.com.example.musiccolab.Protocol;
import main.java.com.example.musiccolab.Server;
import main.java.com.example.musiccolab.exceptions.IPAddressException;

import static org.junit.jupiter.api.Assertions.*;

import main.java.com.example.musiccolab.exceptions.SocketBindException;
import org.junit.jupiter.api.Test;
import test.Client;

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
    void setupServerAddress() throws IPAddressException {
        server.setupServerAddress("localhost", 1200);
        assertNotNull(server.getServerAddressForTesting());

    }

    @Test
    void defineCharTypeTest() throws IPAddressException {
        Server server = new Server();
        server.setupServerAddress("localhost", 1200);
        server.defineCharType();
        assertNotNull(server.getMessageCharsetForTesting());
        assertNotNull(server.getDecoderForTesting());
        assertNotNull(server.getEncoderForTesting());
    }

    @Test
    void openSelectorAndSetupSocket() throws IPAddressException, IOException, SocketBindException {
        Server server = new Server();
        server.setupServerAddress("localhost", 1200);
        server.defineCharType();
        server.OpenSelectorAndSetupSocket();
        assertNotNull(server.getSelectorForTesting());
        assertNotNull(server.getServerChannelForTesting());
    }

    @Test
    void handleConnectionWhenAcceptable() throws IPAddressException, IOException, SocketBindException {
        setupServerAddress("192.168.178.42", 1200);
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
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        String s = thread.result;
        assert thread.result.equals("Welcome in MusicCoLab Server.\r\n");
    }

    @Test
    void handleConnection(){
        Server server = new Server();
        assert(server.isRunningForTesting());
        assert(!server.isFinishedForTesting());
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
}