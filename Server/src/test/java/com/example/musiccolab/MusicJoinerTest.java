package com.example.musiccolab;

import com.example.musiccolab.exceptions.IPAddressException;
import com.example.musiccolab.exceptions.SocketBindException;
import org.junit.jupiter.api.*;
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
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class MusicJoinerTest {

    private Charset messageCharset = null;
    private CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;

    private final Protocol protocol = new Protocol();
    @Test
    void handleToneData() throws IOException, IPAddressException, SocketBindException {
        /*
        SocketChannel testChannel = SocketChannel.open();
        Charset messageCharset = StandardCharsets.US_ASCII;
        Player admin = new Player("Leon", "123", "leon@gmail.com", 0, testChannel);
        Lobby lobby = new Lobby(admin, "lobby1", 0);
        short action = 7;
        byte toneAction = (byte)(action & 0xff);
        int instrument = 1;
        byte toneType = (byte)(instrument & 0xff);
        String toneData = "dataExample2";

        // Test mit korrekten Daten
        assertEquals(0, MusicJoiner.handleToneData(messageCharset, lobby, toneAction, toneType, toneData, action));

        // Test mit lobby = null
        assertEquals(-1, MusicJoiner.handleToneData(messageCharset, null, toneAction, toneType, toneData, action));
        */
        resetProperties();

        //erstelle Server
        setupServerAddress("localhost", 1337);
        defineCharType();
        OpenSelectorAndSetupSocket();

        //erstelle Client
        int action = 7;
        ClientMusicJoiner thread = new ClientMusicJoiner(77, Thread.currentThread(), (short)action);
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
                    protocol.getParseMusicJoiner(messageCharset, (SocketChannel) key.channel(), key);
                    assert protocol.getToneData().equals("dataExample2");
                    assert protocol.getToneAction() == 1;
                    assert protocol.getToneType() == 1;
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

    private void resetProperties(){
        messageCharset = null;
        decoder = null;//Network order = Byte --> Characters = Host order
        encoder = null;//Characters = Host order -->  Network order = Byte
        serverChannel = null;
        serverAddress = null;
        selector = null;
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
}

/*
package test.com.example.musiccolab;

import main.java.com.example.musiccolab.Lobby;
import main.java.com.example.musiccolab.MusicJoiner;
import main.java.com.example.musiccolab.Player;
import main.java.com.example.musiccolab.Server;
import main.java.com.example.musiccolab.exceptions.IPAddressException;
import main.java.com.example.musiccolab.exceptions.SocketBindException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class MusicJoinerTest {

    SocketChannel testChannel;
    Charset messageCharset = StandardCharsets.US_ASCII;
    short action = 7;
    byte toneAction = (byte)(action & 0xff);
    int instrument = 1;
    byte toneType = (byte)(instrument & 0xff);
    String toneData = "dataExample2";
    CharsetDecoder decoder = messageCharset.newDecoder();
    CharsetEncoder encoder = messageCharset.newEncoder();
    String address = "130.149.80.94";
    int port = 8080;
    InetSocketAddress serverAddress = new InetSocketAddress(address, port);


    @Test
    void handleToneData() throws IOException, IPAddressException, SocketBindException {

        // testChannel = SocketChannel.open();

        // admin = new Player("Leon", "123", "leon@gmail.com", 0, testChannel);
        /*
        Server server = new Server();
        server.setupServerAddress(address,port);
        server.defineCharType();
        server.OpenSelectorAndSetupSocket();
        server.handleConnection();
         */
/*
    Selector selector = Selector.open();
        selector.select();
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
        SelectionKey key = (SelectionKey) selectedKeys.next();
        Player player = (Player) key.attachment();
        Lobby lobby = new Lobby(player, "lobby1", 0);
        testChannel = player.getPlayerChannel();

        // Test mit korrekten Daten
        assertEquals(0, MusicJoiner.handleToneData(messageCharset, lobby, toneAction, toneType, toneData, action));

        // Test mit lobby = null
        assertEquals(-1, MusicJoiner.handleToneData(messageCharset, null, toneAction, toneType, toneData, action));

        }

        }
 */