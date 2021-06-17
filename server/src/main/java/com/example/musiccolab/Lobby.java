package main.java.com.example.musiccolab;

import java.util.LinkedList;
import java.util.Random;

public class Lobby {
    private int lobby_id;
    private String lobbyName;
    private int MAX_PLAYERS = 7;
    private LinkedList<Player> players;
    private Player admin;
    private boolean blocking;
    private byte usersNumber = 0;


    public Lobby(Player admin, String lobbyName, int id) {
        this.lobbyName = lobbyName;
        this.lobby_id = id;
        this.players = new LinkedList<>();
        this.admin = admin;
        this.blocking = false;
        this.usersNumber++;

        this.players.add(admin);
        admin.setAdmin(true);
        admin.setLobbyId(id);
        admin.state.setState(ClientState.inLobby);
    }

    public int getLobby_id() { return this.lobby_id; }

    public int getMax_players() { return MAX_PLAYERS; }

    public LinkedList<Player> getPlayers() { return this.players; }

    public boolean setAdmin(Player player) {
        if (playerInLobby(player)) {
            this.admin = player;
            return true;
        }
        return false;
    }

    public Player getAdmin() { return this.admin; }

    public void setBlocking(boolean bool) { this.blocking = bool; }

    public boolean getBlocking() { return this.blocking; }

    public boolean addPlayer(Player player) {
        if (players.size() < MAX_PLAYERS && !blocking && !playerInLobby(player)) {
            players.add(player);
            usersNumber++;
            player.setLobbyId(lobby_id);
            player.state.setState(ClientState.inLobby);
            return true;
        }
        return false;
    }

    public void removePlayer(Player player) {
        if (playerInLobby(player)) {
            players.remove(player);
            usersNumber--;
            player.setLobbyId(-1);
            if (player.getId() == admin.getId()) {
                if (!players.isEmpty()) {
                    admin = players.peek();
                }
                else admin = null;
                player.state.setState(ClientState.notInLobby);
            }
        }
    }

    public boolean playerInLobby(Player player) { return players.contains(player); }

    public byte getUsersNumber(){return usersNumber;}

    public boolean isEmpty(){
        return players.isEmpty();
    }

    public String getPlayerIDs() {
        StringBuilder ids = new StringBuilder();
        for (Player player : players) {
            ids.append(player.getId() + ";");
        }
        return ids.toString();
    }
}
