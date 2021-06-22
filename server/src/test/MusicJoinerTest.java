package test;

import main.java.com.example.musiccolab.Lobby;
import main.java.com.example.musiccolab.MusicJoiner;
import main.java.com.example.musiccolab.Player;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MusicJoinerTest {

    @BeforeEach
    static void initialize() throws IOException {
        SocketChannel testChannel = SocketChannel.open();
        Charset messageCharset = StandardCharsets.US_ASCII;
    }

    @Test
    void handleToneData() throws IOException {

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

    }

    @Test
    void sendTonToClient() throws IOException {
        String message = "testMessage";
        short action = 7;
        SocketChannel testChannel = SocketChannel.open();
        Charset messageCharset = StandardCharsets.US_ASCII;
        // TODO
        // MusicJoiner.sendTonToClient(messageCharset, testChannel, message, action);
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