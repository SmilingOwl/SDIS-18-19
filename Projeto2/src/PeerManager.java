import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class PeerManager {
    private ConcurrentHashMap<Integer, PeerInfo> peers;
    private ConcurrentHashMap<String, ArrayList<Integer>> files;
    private int port;
    private String address;

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

        //TCP Example
        try{
            ServerSocket svc_socket = new ServerSocket(this.port);
            Socket socket = svc_socket.accept();
            byte[] data = new byte[65000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = socket.getInputStream();
            int nRead = 0;
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            byte[] message_data = buffer.toByteArray();
            System.out.println(new String(message_data));
            svc_socket.close();
            socket.close();
        } catch (Exception ex) {
            System.out.println("Error creating socket.");
        }
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
            System.out.println("Usage: java PeerManager <port>");
            return;
        }

        PeerManager p = new PeerManager(Integer.parseInt(args[0]));
    }
}