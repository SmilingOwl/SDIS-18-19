import java.net.InetAddress;
import java.math.BigInteger;

public class PeerInfo {
    private BigInteger id;
    private String address;
    private int port;

    public PeerInfo(BigInteger id, int port, String address) {
        this.id = id;
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