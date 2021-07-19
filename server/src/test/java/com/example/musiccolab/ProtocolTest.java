package com.example.musiccolab;

import com.example.musiccolab.exceptions.IPAddressException;
import com.example.musiccolab.exceptions.SocketBindException;
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

    public static String result = "";


    @Test
    void analyseMainBuffer() throws IPAddressException, IOException, SocketBindException, InterruptedException {

        Thread.sleep(3000);
        setupServerAddress("localhost", 1199);
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
    void ParseForLoginSystemRegister() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1203);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 3;
        protocol.setAction((short) action);

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
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.getParseLogin(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getEmail().equals("zead@gmail.com");
                    assert protocol.getUsername().equals("zead");
                    assert protocol.getPassword().equals("123");
                    counter = 2;
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
        }
    }

    @Test
    void ParseForLoginSystemLogin() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1210);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 1;
        protocol.setAction((short) action);
        Client thread = new Client(10, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.getParseLogin(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getUsername().equals("zead");
                    assert protocol.getPassword().equals("123");
                    counter = 2;
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
        }
    }

    @Test
    void ParseForLoginSystemLogout() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1211);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 2;
        protocol.setAction((short) action);
        Client thread = new Client(11, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.getParseLogin(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getUsername().equals("zead");
                    assert protocol.getPassword().equals("123");
                    counter = 2;
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
        }
    }

    @Test
    void ParseForCreatLobby() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1204);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 4;
        protocol.setAction((short) action);
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
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.setPlayer(new Player("test", "test", "test", -2, (SocketChannel) key.channel()));
                    protocol.getParseLobby(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getLobbyName().equals("example");
                    counter =2;
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
        }
    }

    @Test
    void ParseForJoinLobby() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1212);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 5;
        protocol.setAction((short) action);
        Client thread = new Client(12, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.setPlayer(new Player("test", "test", "test", -2, (SocketChannel) key.channel()));
                    protocol.getParseLobby(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getLobbyID().equals("1");
                    counter =2;
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
        }
    }

    @Test
    void ParseForLeaveLobby() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1213);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 6;
        protocol.setAction((short) action);
        Client thread = new Client(13, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.setPlayer(new Player("test", "test", "test", -2, (SocketChannel) key.channel()));
                    protocol.getParseLobby(messageCharset, (SocketChannel) key.channel(), key);
                    counter =2;
                }
                selectedKeys.remove();
            }

            try {
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait();
                    if(counter == 2)
                        assert result.substring(result.indexOf("e")).equals("either lobby is full or lobbyId is wrong or you are already in.");
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
        }
    }

    @Test
    void parseBufferForMusicJoiner() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1214);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 7;
        protocol.setAction((short) action);
        Client thread = new Client(14, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.setPlayer(new Player("test", "test", "test", -2, (SocketChannel) key.channel()));
                    protocol.getParseMusicJoiner(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getToneData().equals("dataExample2");
                    counter = 2;
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
        }
    }

    @Test
    void handleActionLogin() throws IOException, IPAddressException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1215);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 1;
        protocol.setAction((short) action);
        Client thread = new Client(15, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                   protocol.handleAction(messageCharset, (SocketChannel) key.channel(), 7, key);
                   assert protocol.test();
                    counter = 2;
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
        }
    }

    @Test
    void handleActionCreateLobby() throws IOException, IPAddressException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1216);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 4;
        protocol.setAction((short) action);
        Client thread = new Client(16, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.handleAction(messageCharset, (SocketChannel) key.channel(), 7, key);
                    assert protocol.test();
                    counter = 2;
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
        }
    }

    @Test
    void handleActionMusicJoiner() throws IOException, IPAddressException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1217);
        defineCharType();
        OpenSelectorAndSetupSocket();

        int action = 7;
        protocol.setAction((short) action);
        Client thread = new Client(17, Thread.currentThread(), (short)action);
        thread.start();

        int counter = 0;
        while (counter < 2) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.handleAction(messageCharset, (SocketChannel) key.channel(), 7, key);
                    assert protocol.test();
                    counter = 2;
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
        }
    }

    @Test
    void readSizes() throws IPAddressException, IOException, SocketBindException, InterruptedException {
        resetProperties();
        Thread.sleep(3000);

        setupServerAddress("localhost", 1205);
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
                    counter = 1;

                } else if (key.isReadable()) {
                    protocol.analyseMainBuffer(messageCharset, (SocketChannel) key.channel());
                    protocol.getSize(messageCharset, (SocketChannel) key.channel());
                    assert protocol.getEmailSize() == 14;
                    assert protocol.getUserNameSize() == 4;
                    assert protocol.getPasswordSize() == 3;
                    counter = 2;
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
        }
    }

    @Test
    void convertShortToByte() {
        short value = 55;
        byte[] returnValue = protocol.convertShortToByte(value);
        for(byte b : returnValue)
            assert b >= 0;
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