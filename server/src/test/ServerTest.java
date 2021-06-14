package test;

import main.java.com.example.musiccolab.Server;
import main.java.com.example.musiccolab.exceptions.IPAddressException;

import static org.junit.jupiter.api.Assertions.*;

import main.java.com.example.musiccolab.exceptions.SocketBindException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class ServerTest {

    @Test
    void setupServerAddress() throws IPAddressException {
        Server server = new Server();
        server.setupServerAddress("localhost", 1200);
        assertNotNull(server.getServerAddressForTesting());

    }

    @Test
    void defineCharType() throws IPAddressException {
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
}