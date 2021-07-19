package com.example.musiccolab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

class PlayerTest {
    Player player;
    String name = "test";
    String passwort = "starkes-passwort";
    String email = "test@gmail.com";
    int id = 0;

    @BeforeEach
    public void setUp() throws IOException {
        SocketChannel channel = SocketChannel.open();
        player = new Player(name, passwort, email, id, channel);
    }

    @Test
    public void testAddress() {
        InetSocketAddress address = new InetSocketAddress("localhsot", 8080);
        player.setAddress(address);
        assertTrue(player.getAddress().equals(address));
    }

    @Test
    public void testName() {
        assertEquals(player.getName(), name);
    }

    @Test
    public void testPasswort() {
        assertEquals(player.getPasswort(), passwort);
        player.setPasswort("neues-besseres-passwort");
        assertEquals(player.getPasswort(), "neues-besseres-passwort");
    }

    @Test
    public void testEmail() {
        assertEquals(player.getEmail(), email);
    }

    @Test
    public void testID() {
        assertEquals(player.getId(), id);
    }

    @Test
    public void testAdmin() {
        assertFalse(player.isAdmin());
        player.setAdmin();
        assertTrue(player.isAdmin());
    }

    @Test
    public void testLogin() {
        assertFalse(player.isLoggedIn());
        player.setLoggedIn();
        assertTrue(player.isLoggedIn());
        player.setLoggedOut();
        assertFalse(player.isLoggedIn());
    }

    @Test
    public void testLobbyID() {
        assertEquals(player.getLobbyId(), -1);
        player.setLobbyId(25);
        assertEquals(player.getLobbyId(), 25);
    }

    @AfterEach
    public void closeChannel() throws IOException {
        player.getPlayerChannel().close();
    }

}
