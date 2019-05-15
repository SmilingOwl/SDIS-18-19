import java.net.InetAddress;

public class PeerInfo {
    private String address;
    private int port;

    public PeerInfo(int port, String address) {
        this.port = port;
        this.address = address;
    }

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }
}