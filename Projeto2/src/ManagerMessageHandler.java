import javax.net.ssl.SSLSocket;
import java.net.*;
import java.io.*;
import java.util.*;

public class ManagerMessageHandler implements Runnable {
    private Message message;
    private PeerManager peer_manager;
    private SSLSocket socket;
    
    public ManagerMessageHandler(PeerManager peer_manager, SSLSocket socket, byte[] msg) {
        this.peer_manager = peer_manager;
        this.message = new Message(msg);
        this.socket = socket;
    }

    public void run() {
        System.out.println("Entered manager.");
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
        if(this.peer_manager.get_peers().get(peer_id) != null) {
            System.out.println("Error: Peer with id " + peer_id + " already exists in the system.");
        } else {
            PeerInfo peer_info = new PeerInfo(port, address);
            this.peer_manager.get_peers().put(peer_id, peer_info);
            System.out.println("Peer " + peer_id + " joined the system.");
        }
        //socket DONE or ERROR messages?
    }

    public void backup_request() {
        System.out.println("Received backup request.");
        int rep_degree = message.get_rep_degree();
        //TODO build message
        String message = "B_AVAILABLE localhost 1 localhost 2 \r\n\r\n";
        try {
            socket.getOutputStream().write(message.getBytes());
            System.out.println("Sent message.");
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
    }
}