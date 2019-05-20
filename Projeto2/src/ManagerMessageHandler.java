import javax.net.ssl.SSLSocket;
import java.util.ArrayList;
import java.io.InputStream;

public class ManagerMessageHandler implements Runnable {
    private Message message;
    private PeerManager owner;
    private SSLSocket socket;
    
    public ManagerMessageHandler(PeerManager owner, SSLSocket socket, byte[] msg) {
        this.owner = owner;
        this.message = new Message(msg);
        this.socket = socket;
        System.out.println("Received message: " + new String(msg));
    }

    public void run() {
        if (this.message.get_type().equals("JOIN")) {
            this.peer_join();
        } else if (this.message.get_type().equals("BACKUP")) {
            this.backup_request();
        } else if (this.message.get_type().equals("STORED")) {
            this.stored_message(false);
        } else if (this.message.get_type().equals("STORED_M")) {
            this.stored_message(true);
        } else if (this.message.get_type().equals("RESTORE")) {
            this.restore_request();
        } else if (this.message.get_type().equals("DELETE")) {
            this.delete_request();
        } else if (this.message.get_type().equals("DELETED")) {
            this.deleted_message();
        } else if (this.message.get_type().equals("MANAGER_ADD")) {
            this.manager_add();
        } else if (this.message.get_type().equals("MANAGER_JOIN")) {
            this.manager_join();
        } else if (this.message.get_type().equals("ACTIVE")) {
            this.peer_active(false);
        } else if (this.message.get_type().equals("ACTIVE_M")) {
            this.peer_active(true);
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

    private void stored_message(boolean sent_by_manager) {
        int peer_id = message.get_peer_id();
        String file_id = message.get_file_id();
        ArrayList<Integer> peers_backing_up_file = this.owner.get_files().get(file_id);
        PeerInfo peer_info = this.owner.get_peers().get(peer_id);
        peer_info.increase_count_files();
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

        if(!sent_by_manager){
            for (int i = 0; i < this.owner.get_managers().size(); i++) {
                this.message.set_type("STORED_M");
                SendMessage redirect_message = new SendMessage(this.owner.get_managers().get(i).get_address(),
                    this.owner.get_managers().get(i).get_port(), this.message, this.owner.get_context().getSocketFactory());
                    redirect_message.run();
            }
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

    public void delete_request() {
        System.out.println("\nReceived delete request.");
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

    private void deleted_message() {
        //TODO delete peer id from files hashmap, where file_id is the one received in the message.
        //if arraylist corresponding to the file_id in the hashmap is empty, delete file_id from hashmap.
    }

    private void manager_add() {
        String address = message.get_address();
        int port = message.get_port();
        PeerManagerInfo new_manager = new PeerManagerInfo(port, address);
        this.owner.get_managers().add(new_manager);
    }

    private void manager_join() {
        String address = message.get_address();
        int port = message.get_port();

        Message peer_info = new Message("PEER_INFO", -1, null, this.owner.get_peers().size(), null, null, -1, null);
        try {
            this.socket.getOutputStream().write(peer_info.build());
            for(Integer peer_id : this.owner.get_peers().keySet()) {
                PeerInfo peer = this.owner.get_peers().get(peer_id);
                Message peer_msg = new Message("PEER", peer_id, null, peer.get_count_files(), null, peer.get_address(), peer.get_port(), null);
                this.socket.getOutputStream().write(peer_msg.build());
                InputStream istream = socket.getInputStream();
                byte[] data = new byte[3];
                istream.read(data, 0, data.length);
                String ack = new String(data);
                if(!ack.equals("ACK")) {
                    System.out.println("Error sending peer info answering manager join request.");
                    break;
                }
            }
            Message file_info = new Message("FILE_INFO", -1, null, this.owner.get_files().size(), null, null, -1, null);
            this.socket.getOutputStream().write(file_info.build());
            for(String file_id : this.owner.get_files().keySet()) {
                Message file_msg = new Message("FILE_P", file_id, this.owner.get_files().get(file_id));
                this.socket.getOutputStream().write(file_msg.build());
                InputStream istream = socket.getInputStream();
                byte[] data = new byte[3];
                istream.read(data, 0, data.length);
                String ack = new String(data);
                if(!ack.equals("ACK")) {
                    System.out.println("Error sending peer info answering manager join request.");
                    break;
                }
            }
            ArrayList<PeerInfo> managers = new ArrayList<PeerInfo>();
            for(int i = 0; i < this.owner.get_managers().size(); i++) {
                PeerInfo manager_info = new PeerInfo(-1, this.owner.get_managers().get(i).get_port(), 
                    this.owner.get_managers().get(i).get_address());
                managers.add(manager_info);
            }
            Message manager_info = new Message("MANAGER_INFO", -1, null, -1, null, null, -1, managers);
            this.socket.getOutputStream().write(manager_info.build());
        } catch (Exception ex) {
            System.out.println("Error sending manager info messages.");
            ex.printStackTrace();
        }
        PeerManagerInfo new_manager = new PeerManagerInfo(port, address);
        this.owner.get_managers().add(new_manager);
    }

    public void peer_active(boolean sent_by_manager) {
        int peer_id = this.message.get_peer_id();
        PeerInfo peer = this.owner.get_peers().get(peer_id);
        peer.set_time(System.currentTimeMillis());
        if(!sent_by_manager){
            for (int i = 0; i < this.owner.get_managers().size(); i++) {
                this.message.set_type("ACTIVE_M");
                SendMessage redirect_message = new SendMessage(this.owner.get_managers().get(i).get_address(),
                    this.owner.get_managers().get(i).get_port(), this.message, this.owner.get_context().getSocketFactory());
                    redirect_message.run();
            }
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
                        min = available_peer.get_count_files();
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