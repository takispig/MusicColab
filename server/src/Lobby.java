import java.util.LinkedList;

public class Lobby {
    private int lobby_id;
    private int max_players;
    private LinkedList<Player> players;
    private Player admin;
    private boolean blocking;

    public Lobby(int lobby_id, int max_players, Player admin) {
        this.lobby_id = lobby_id;
        this.max_players = max_players;
        this.players = new LinkedList<>();
        this.admin = admin;
        this.blocking = false;

        this.players.add(admin);
    }

    public int getLobby_id() { return this.lobby_id; }

    public int getMax_players() { return this.max_players; }

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
        if (players.size() < max_players && !blocking && !playerInLobby(player)) {
            players.add(player);
            return true;
        }
        return false;
    }

    public void removePlayer(Player player) {
        if (playerInLobby(player)) {
            players.remove(player);
            if (player.getId() == admin.getId()) {
                if (!players.isEmpty()) {
                    admin = players.peek();
                }
                else admin = null;
            }
        }
    }

    public boolean playerInLobby(Player player) { return players.contains(player); }


}
