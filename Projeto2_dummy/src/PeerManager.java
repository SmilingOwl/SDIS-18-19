import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.ArrayList;
import java.net.InetAddress;

public class PeerManager {
    private ConcurrentHashMap<Integer, PeerInfo> peers;
    private ConcurrentHashMap<String, ArrayList<Integer>> files;
    private int port;
    private String address;
    private ScheduledThreadPoolExecutor thread_executor;

    PeerManager(int port) {
        this.port = port;
        try{
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception ex) {
            System.out.println("Error getting address.");
            return;
        }
        this.peers = new ConcurrentHashMap<Integer, PeerInfo>();
        this.files = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.thread_executor = new ScheduledThreadPoolExecutor(3000);

        this.thread_executor.execute(new ConnectionThread(this.port, this.thread_executor, this));
    }

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }

    public ConcurrentHashMap<String, ArrayList<Integer>> get_files() {
        return this.files;
    }

    public ConcurrentHashMap<Integer, PeerInfo> get_peers() {
        return this.peers;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password PeerManager <port>");
            return;
        }

        PeerManager p = new PeerManager(Integer.parseInt(args[0]));
    }
}