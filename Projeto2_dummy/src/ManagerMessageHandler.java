import javax.net.ssl.SSLSocket;
import java.net.*;
import java.io.*;
import java.util.*;

public class ManagerMessageHandler implements Runnable {
    private Message message;
    private PeerManager owner;
    private SSLSocket socket;
    
    public ManagerMessageHandler(PeerManager owner, SSLSocket socket, byte[] msg) {
        this.owner = owner;
        this.message = new Message(msg);
        this.socket = socket;
    }

    public void run() {
        if (this.message.get_type().equals("JOIN")) {
            this.peer_join();
        } else if (this.message.get_type().equals("BACKUP")) {
            this.backup_request();
        }
    }

    public void peer_join() {
        int peer_id = this.message.get_peer_id();
        int port = this.message.get_port();
        String address = this.message.get_address();
        if(this.owner.get_peers().get(peer_id) != null) {
            System.out.println("Error: Peer with id " + peer_id + " already exists in the system.");
        } else {
            PeerInfo peer_info = new PeerInfo(peer_id, port, address);
            this.owner.get_peers().put(peer_id, peer_info);
            System.out.println("Peer " + peer_id + " joined the system.");
        }
        //socket DONE or ERROR messages?
    }

    public void backup_request() {
        System.out.println("Received backup request.");
        int peer_id = this.message.get_peer_id();
        int rep_degree = message.get_rep_degree();
        ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
        int n = 0;
        //improve this by getting the peers with fewer files stored TODO
        for(Integer id : this.owner.get_peers().keySet()) {
            if(n >= rep_degree)
                break;
            if(id != peer_id) {
                peers.add(this.owner.get_peers().get(id));
            }
        }
        Message message = new Message("B_AVAILABLE", -1, null, rep_degree, null, null, -1, peers);
        try {
            socket.getOutputStream().write(message.build());
            System.out.println("Sent message.");
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
    }
}