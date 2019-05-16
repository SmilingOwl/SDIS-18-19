import java.net.InetAddress;

public class PeerInfo {
    private int id;
    private String address;
    private int port;

    public PeerInfo(int id, int port, String address) {
        this.id = id;
        this.port = port;
        this.address = address;
    }

    public int get_id() {
        return this.id;
    }

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }
}