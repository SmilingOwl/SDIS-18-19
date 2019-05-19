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
        } else if (this.message.get_type().equals("STORED")) {
            this.stored_message();
        } else if (this.message.get_type().equals("RESTORE")) {
            this.restore_request();
        }
    }

    /*************** Message Handler Functions ***************/
    public void peer_join() {
        int peer_id = this.message.get_peer_id();
        int port = this.message.get_port();
        String address = this.message.get_address();
        String answer = "ACK";
        if(this.owner.get_peers().get(peer_id) != null) {
            answer = "ERROR ID";
            System.out.println("Error: Peer with id " + peer_id + " already exists in the system.");
        } else {
            PeerInfo peer_info = new PeerInfo(peer_id, port, address);
            this.owner.get_peers().put(peer_id, peer_info);
            System.out.println("Peer " + peer_id + " joined the system.");
        }
        try {
            this.socket.getOutputStream().write(answer.getBytes());
        } catch (Exception ex) {
            System.out.println("Error communicating with peer.");
        }
    }

    public void backup_request() {
        System.out.println("\nReceived backup request.");
        int peer_id = this.message.get_peer_id();
        int rep_degree = message.get_rep_degree();
        ArrayList<PeerInfo> peers = get_peers_with_fewer_files(rep_degree, peer_id);
        Message message = new Message("AVAILABLE", -1, null, rep_degree, null, null, -1, peers);
        try {
            socket.getOutputStream().write(message.build());
            System.out.println("Sent available message.");
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
    }

    private void stored_message() {
        int peer_id = message.get_peer_id();
        String file_id = message.get_file_id();
        ArrayList<Integer> peers_backing_up_file = this.owner.get_files().get(file_id);
        if(peers_backing_up_file == null) {
            ArrayList<Integer> new_peers = new ArrayList<Integer>();
            new_peers.add(peer_id);
            this.owner.get_files().put(file_id, new_peers);
        } else {
            peers_backing_up_file.add(peer_id);
        }
        
        System.out.println("\nPeer " + peer_id + " is backing file with id " + file_id);
        //DEBUG TODO delete
        for(String key : this.owner.get_files().keySet()) {
            System.out.print("key - ");
            for(int i = 0; i < this.owner.get_files().get(key).size(); i++) {
                System.out.println(this.owner.get_files().get(key).get(i) + " ; ");
            }
            System.out.println();
        }
    }

    public void restore_request() {
        System.out.println("\nReceived restore request.");
        String file_id = this.message.get_file_id();
        ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
        for(int i = 0; i < this.owner.get_files().get(file_id).size(); i++) {
            PeerInfo peer = this.owner.get_peers().get(this.owner.get_files().get(file_id).get(i));
            peers.add(peer);
        }
        Message message = new Message("AVAILABLE", -1, null, -1, null, null, -1, peers);
        try {
            socket.getOutputStream().write(message.build());
            System.out.println("Sent available message.");
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
    }

    /*************** Auxiliary Functions ***************/
    private ArrayList<PeerInfo> get_peers_with_fewer_files(int rep_degree, int peer_id) {
        ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
        int max_peers = rep_degree;
        if(this.owner.get_peers().size() - 1 < rep_degree) {
            max_peers = this.owner.get_peers().size() - 1;
        }

        while(peers.size() < max_peers) {
            int min = Integer.MAX_VALUE;
            PeerInfo min_peer = null;
            for(PeerInfo available_peer : this.owner.get_peers().values()) {
                if(available_peer.get_id() != peer_id && !peers.contains(available_peer)) {
                    if(available_peer.get_count_files() < min) {
                        min_peer = available_peer;
                    }
                }
            }
            if(min_peer == null) {
                System.out.println("Error: On getting fewer files.");
                break;
            }
            peers.add(min_peer);
        }

        return peers;
    }
}