import javax.net.ssl.SSLSocket;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.File;

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
            this.peer_join(false);
        } else if (this.message.get_type().equals("JOIN_M")) {
            this.peer_join(true);
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
            this.deleted_message(false);
        } else if (this.message.get_type().equals("DELETED_M")) {
            this.deleted_message(true);
        } else if (this.message.get_type().equals("RECLAIM")) {
            this.reclaim_message(false);
        } else if (this.message.get_type().equals("RECLAIM_M")) {
            this.reclaim_message(true);
        } else if (this.message.get_type().equals("B_RECLAIM")) {
            this.reclaim_request();
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
    public void peer_join(boolean sent_by_manager) {
        int peer_id = this.message.get_peer_id();
        int port = this.message.get_port();
        String address = this.message.get_address();
        
        if(!sent_by_manager) {
            try {
                if(this.owner.get_peers().get(peer_id) != null) {
                    String answer = "ERROR ID";
                    System.out.println("Error: Peer with id " + peer_id + " already exists in the system.");
                    this.socket.getOutputStream().write(answer.getBytes());
                } else {
                    PeerInfo peer_info = new PeerInfo(peer_id, port, address);
                    this.owner.get_peers().put(peer_id, peer_info);
                    System.out.println("Peer " + peer_id + " joined the system.");
                    ArrayList<PeerInfo> managers = new ArrayList<PeerInfo>();
                    for(int i = 0; i < this.owner.get_managers().size(); i++) {
                        PeerInfo manager_info = new PeerInfo(-1, this.owner.get_managers().get(i).get_port(), 
                            this.owner.get_managers().get(i).get_address());
                        managers.add(manager_info);
                    }
                    Message manager_info = new Message("MANAGER_INFO", -1, null, -1, null, null, -1, managers);
                    this.socket.getOutputStream().write(manager_info.build());
                }
            } catch (Exception ex) {
                System.out.println("Error communicating with peer.");
            }
            for (int i = 0; i < this.owner.get_managers().size(); i++) {
                this.message.set_type("JOIN_M");
                SendMessage redirect_message = new SendMessage(this.owner.get_managers().get(i).get_address(),
                    this.owner.get_managers().get(i).get_port(), this.message, this.owner.get_context().getSocketFactory());
                redirect_message.run();
            }
        } else {
            PeerInfo peer_info = new PeerInfo(peer_id, port, address);
            this.owner.get_peers().put(peer_id, peer_info);
            System.out.println("Peer " + peer_id + " joined the system.");
        }
    }

    public void backup_request() {
        System.out.println("\nReceived backup request.");
        int peer_id = this.message.get_peer_id();
        int rep_degree = message.get_rep_degree();
        int occupied = message.get_port();
        ArrayList<PeerInfo> peers = get_peers_more_space(rep_degree, peer_id, occupied);
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
        int free_space = message.get_rep_degree();
        ArrayList<Integer> peers_backing_up_file = this.owner.get_files().get(file_id);
        PeerInfo peer_info = this.owner.get_peers().get(peer_id);
        peer_info.increase_count_files();
        peer_info.set_free_space(free_space);
        if(peers_backing_up_file == null) {
            ArrayList<Integer> new_peers = new ArrayList<Integer>();
            new_peers.add(peer_id);
            this.owner.get_files().put(file_id, new_peers);
        } else {
            if(!peers_backing_up_file.contains(peer_id))
                peers_backing_up_file.add(peer_id);
        }
        
        System.out.println("\nPeer " + peer_id + " is backing file with id " + file_id);
        
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

    private void deleted_message(boolean sent_by_manager) {

        String file_id = this.message.get_file_id();
        int peer_id= this.message.get_peer_id();
        int free_space = this.message.get_rep_degree();
        PeerInfo peer_info = this.owner.get_peers().get(peer_id);
        peer_info.decrease_count_files();
        peer_info.set_free_space(free_space);
        
        if(this.owner.get_files().get(file_id)!= null){
            for(int i=0; i< this.owner.get_files().get(file_id).size(); i++){
                if(this.owner.get_files().get(file_id).get(i) == peer_id ){
                    this.owner.get_files().get(file_id).remove(i);
                    i--;
                }
            }
            if(this.owner.get_files().get(file_id).size() == 0){
                this.owner.get_files().remove(file_id);
            }
        }
        if(!sent_by_manager){
            for (int i = 0; i < this.owner.get_managers().size(); i++) {
                this.message.set_type("DELETED_M");
                SendMessage redirect_message = new SendMessage(this.owner.get_managers().get(i).get_address(),
                    this.owner.get_managers().get(i).get_port(), this.message, this.owner.get_context().getSocketFactory());
                    redirect_message.run();
            }
        }
    }

    private void reclaim_message(boolean sent_by_manager) {
        int peer_id = this.message.get_peer_id();
        int free_space = this.message.get_rep_degree();
        PeerInfo peer_info = this.owner.get_peers().get(peer_id);
        peer_info.set_free_space(free_space);

        if(!sent_by_manager){
            for (int i = 0; i < this.owner.get_managers().size(); i++) {
                this.message.set_type("RECLAIM_M");
                SendMessage redirect_message = new SendMessage(this.owner.get_managers().get(i).get_address(),
                    this.owner.get_managers().get(i).get_port(), this.message, this.owner.get_context().getSocketFactory());
                    redirect_message.run();
            }
        }
    }

    private void reclaim_request() {
        String file_id = this.message.get_file_id();
        int occupied = this.message.get_rep_degree();
        ArrayList<PeerInfo> peers = get_peers_without_file(file_id, occupied);
        Message message = new Message("AVAILABLE", -1, null, -1, null, null, -1, peers);
        try {
            socket.getOutputStream().write(message.build());
            System.out.println("Sent available message.");
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
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
                Message peer_msg = new Message("PEER", peer_id, null, peer.get_free_space(), null, peer.get_address(), peer.get_port(), null);
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
    private ArrayList<PeerInfo> get_peers_more_space(int rep_degree, int peer_id, int occupied) {
        ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
        int max_peers = rep_degree;
        if(this.owner.get_peers().size() - 1 < rep_degree) {
            max_peers = this.owner.get_peers().size() - 1;
        }

        while(peers.size() < max_peers) {
            int max = Integer.MIN_VALUE;
            PeerInfo max_peer = null;
            for(PeerInfo available_peer : this.owner.get_peers().values()) {
                if(available_peer.get_id() != peer_id && !peers.contains(available_peer)) {
                    if(available_peer.get_free_space() > max && available_peer.get_free_space() > occupied) {
                        max_peer = available_peer;
                        max = available_peer.get_free_space();
                    }
                }
            }
            if(max_peer == null) {
                System.out.println("Error: couldn't get enough peers with space.");
                break;
            }
            peers.add(max_peer);
        }

        return peers;
    }

    private ArrayList<PeerInfo> get_peers_without_file(String file_id, int occupied) {
        ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
        ArrayList<Integer> peers_with_file = this.owner.get_files().get(file_id);

        for(Integer peer_id : this.owner.get_peers().keySet()) {
            if(!peers_with_file.contains(peer_id) && this.owner.get_peers().get(peer_id).get_free_space() > occupied) {
                peers.add(this.owner.get_peers().get(peer_id));
            }
        }

        return peers;
    }
}