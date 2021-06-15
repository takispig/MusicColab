package main.java.com.example.musiccolab;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class Player {
    private String name;
    private String passwort;
    private String email;
    private int id;
    private int lobbyId = -1;
    private boolean loggedIn = false;
    private boolean admin = true;
    private SocketAddress address;
    private SocketChannel channel;
    public ClientState state = new ClientState();

    public Player(String name, String passwort, String email, int id, SocketChannel channel) throws IOException {
        this.name = name;
        this.passwort = passwort;
        this.email = email;
        this.id = id;
        this.channel = channel;
        this.address = channel.getRemoteAddress();
    }


    public SocketChannel getPlayerChannel(){ return this.channel; }

    public void setAddress(InetSocketAddress address){
        this.address = address;
    }

    public String getName() { return this.name; }

    public String getPasswort() { return this.passwort; }

    public void setPasswort(String passwort) { this.passwort = passwort; }

    public String getEmail() { return this.email; }

    public int getId() { return this.id; }

    public SocketAddress getAddress() { return this.address; }

    public boolean isAdmin(){return admin;}

    public void setAdmin(){admin = true;}

    public boolean isLoggedIn(){return loggedIn;}

    public void setLoggedIn(){loggedIn = true;}
    public void setLoggedOut(){loggedIn = false;}

    public void setLobbyId(int id){lobbyId = id;}
    public int getLobbyId(){return lobbyId;}

}
