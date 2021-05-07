import java.util.LinkedList;

public class Lobby {

    private int lobby_id;//lobbyId is as String needed.
    private int max_players;
    private LinkedList<Player> players;
    private Player admin;
    private boolean blocking;

    public Lobby(int max_players, Player admin) {
        this.lobby_id = lobby_id;
        this.max_players = max_players;
        this.players = new LinkedList<>();
        this.admin = admin;
        this.blocking = false;

        this.players.add(admin);
    }

    public void removePlayer(Player player) {}
    public boolean addPlayer(Player player){return true;}
}
