package com.example.musiccolab;

import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Random;

public class Lobby {
    private int lobby_id;

    public String getLobbyName() {
        return lobbyName;
    }

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
        admin.setAdmin();
        admin.setLobbyId(id);
        admin.state.setState(ClientState.inLobby);
    }

    public int getLobby_id() { return this.lobby_id; }

    public int getMax_players() { return MAX_PLAYERS; }

    public LinkedList<Player> getPlayers() { return this.players; }

    public boolean setAdmin(Player player) {
        if (playerInLobby(player)) {
            this.admin = player;
            player.setAdmin();
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
            player.state.setState(ClientState.notInLobby);
            usersNumber--;
            player.setLobbyId(-1);
            if (player.isAdmin()) {
                if (!players.isEmpty()) {
                    setAdmin(players.getFirst());
                }
                else admin = null;
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

    public void toggleMutePlayerByID(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                player.toggleMute();
                System.out.println("Player: " + player.getName() + ", " + player.getId() + " is now muted: " + player.isMuted());
                break;
            }
        }
    }

    public void toggleMutePlayerByUsername(String username) {
        for (Player player : players) {
            if (player.getName().equals(username)) {
                player.toggleMute();
                System.out.println("Player: " + player.getName() + ", " + player.getId() + " is now muted: " + player.isMuted());
                break;
            }
        }
    }

    public static boolean lobbyNameExist(String name){
        for(Lobby l : Server.lobbyList){
            if(l.lobbyName.equals(name))
                return true;
        }
        return false;
    }

    public String getPlayersListAsString(){
        String playersAsString = "";
        for(Player player : players)
            playersAsString += player.getName() + ",";
        return playersAsString;
    }
}
