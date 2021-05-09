package src;

import java.util.LinkedList;

public class Game {

    private LinkedList<Player> players;
    private Lobby lobby;
    public Game(Lobby lobby){
        this.lobby = lobby;
        this.players = lobby.getPlayers();
    }

}
