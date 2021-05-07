import java.net.SocketAddress;

public class Player {
    private String name;
    private String passwort;
    private String email;
    private int id;
    private SocketAddress address;//Here is SocketAddress needed not InetSocketAddress.

    public Player(String name, String passwort, String email, int id, SocketAddress address) {
        this.name = name;
        this.passwort = passwort;
        this.email = email;
        this.id = id;
        this.address = address;
    }
}
