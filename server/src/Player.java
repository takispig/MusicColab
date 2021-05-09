import java.net.InetSocketAddress;

public class Player {
    private String name;
    private String passwort;
    private String email;
    private int id;
    private InetSocketAddress address;

    public Player(String name, String passwort, String email, int id, InetSocketAddress address) {
        this.name = name;
        this.passwort = passwort;
        this.email = email;
        this.id = id;
        this.address = address;
    }

    public String getName() { return this.name; }

    public String getPasswort() { return this.passwort; }

    public void setPasswort(String passwort) { this.passwort = passwort; }

    public String getEmail() { return this.email; }

    public int getId() { return this.id; }

    public InetSocketAddress getAddress() { return this.address; }

}
