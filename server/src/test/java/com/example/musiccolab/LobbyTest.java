package com.example.musiccolab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {
    Lobby lobby;
    Player admin;
    String lobbyName = "squad";
    int id = 0;
    Player player1;
    Player player2;
    Player player3;
    Player player4;

    @BeforeEach
    public void setUp() throws IOException {
        SocketChannel channel = SocketChannel.open();
        admin = new Player("admin", "passwort", "admin@gmail.com", 0, channel);
        lobby = new Lobby(admin, lobbyName, id);
        player1 = new Player("test1", "passwort1", "test1@gmail.com", 1, channel);
        player2 = new Player("test2", "passwort2", "test2@gmail.com", 2, channel);
        player3 = new Player("test3", "passwort3", "test3@gmail.com", 3, channel);
        player4 = new Player("test4", "passwort4", "test4@gmail.com", 4, channel);
    }

    @Test
    public void testLobbyID() {
        assertEquals(lobby.getLobby_id(), id);
    }

    @Test
    public void testMaxPlayers() {
        assertEquals(lobby.getMax_players(), 7);
    }

    @Test
    public void testAddPlayers() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);

        LinkedList<Player> players = lobby.getPlayers();
        for (Player player : players) {
            assertNotNull(player);
        }

        assertEquals(lobby.getUsersNumber(), 5);
    }

    @Test
    public void testAdmin() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);
        assertEquals(lobby.getAdmin(), admin);
        lobby.setAdmin(player1);
        assertEquals(lobby.getAdmin(), player1);
    }

    @Test
    public void testBlocking() {
        assertFalse(lobby.getBlocking());
        lobby.setBlocking(true);
        assertTrue(lobby.getBlocking());
    }

    @Test
    public void testRemovePlayers() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);

        assertTrue(lobby.playerInLobby(player3));
        lobby.removePlayer(player3);
        assertFalse(lobby.playerInLobby(player3));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(lobby.playerInLobby(admin));
        lobby.removePlayer(admin);
        assertTrue(lobby.isEmpty());
    }

    @Test
    public void testGetPlayerIDs() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);

        String str = admin.getId() + ";" + player1.getId() + ";" + player2.getId() + ";";
        assertEquals(lobby.getPlayerIDs(), str);
    }

    @AfterEach
    public void closeAdminChannel() throws IOException {
        admin.getPlayerChannel().close();
    }
}
