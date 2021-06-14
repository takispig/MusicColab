package tests;

import org.junit.jupiter.api.Test;
import src.Lobby;
import src.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.*;



class LobbyTest {
    SocketChannel channel = null;
    Player player = new Player("Nils", "1234", "nilsneurath@gmail.com", 1,channel);

    LobbyTest() throws IOException {
    }

    @Test
    void getLobby_id() {
        Lobby testLobby1 = new Lobby(player);
        Lobby testLobby2 = new Lobby(player);

        assertEquals(1,testLobby1.getLobby_id());
        assertEquals(2,testLobby2.getLobby_id());
    }
}