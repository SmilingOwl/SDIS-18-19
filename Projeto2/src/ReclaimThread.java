import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ReclaimThread implements Runnable {
    private Peer owner;

    public ReclaimThread(Peer owner) {
        this.owner = owner;
    }

    public void run() {
        this.send_initial_message();
        File backup_dir = new File("peer" + this.owner.get_id() + "/backup");
        while(this.owner.get_free_space() < 0) {
            SaveFile file = new SaveFile("peer" + this.owner.get_id() + "/backup/" + backup_dir.listFiles()[0].getName(), 1);
            String file_id = backup_dir.listFiles()[0].getName();
            Message reclaim_message = new Message("B_RECLAIM", this.owner.get_id(), file_id, (int) backup_dir.listFiles()[0].length(), null, null, this.owner.get_free_space(), null); 
            Message manager_answer = this.reclaim_request_manager(reclaim_message);
            ArrayList<PeerInfo> address_list = manager_answer.get_peers();
            Message to_peer = new Message("P2P_BACKUP", this.owner.get_id(), file_id, file.get_body().size(),
                null, null, -1, null);
            this.reclaim_request(to_peer, address_list, file);
            this.delete_file(file_id);
        }

        System.out.println("At the end of RECLAIM protocol, I have " + this.owner.get_free_space() + " free space to save files.");
    }

    public void send_initial_message() {
        Message message = new Message("RECLAIM", this.owner.get_id(), null, this.owner.get_free_space(), null, null, -1, null);
        int i = 0;
        while(i < this.owner.get_managers().size())
        {
            try {
                SSLSocketFactory socketfactory = this.owner.get_context().getSocketFactory();
                SSLSocket socket = (SSLSocket) socketfactory.createSocket(this.owner.get_manager_address(), this.owner.get_manager_port());
                socket.getOutputStream().write(message.build());
                System.out.println("Sent reclaim message to manager.");
                break;
            } catch(Exception Ex) {
                System.out.println("Error connecting to manager. Trying another one.");
                this.owner.switch_manager();
                i++;
            }
        }

        if(i == this.owner.get_managers().size()) {
            System.out.println("Couldn't connect to any manager.");
        }
    }

    public Message reclaim_request_manager(Message message) {
        Message received_message = null;
        int i = 0;
        while(i < this.owner.get_managers().size())
        {
            try {
                SSLSocketFactory socketfactory = this.owner.get_context().getSocketFactory();
                SSLSocket socket = (SSLSocket) socketfactory.createSocket(this.owner.get_manager_address(), this.owner.get_manager_port());
                socket.getOutputStream().write(message.build());
                System.out.println("Sent backup request to manager.");
                byte[] data = new byte[16000];
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream stream = socket.getInputStream();
                int nRead = stream.read(data, 0, data.length);
                buffer.write(data, 0, nRead);
                byte[] message_data = buffer.toByteArray();
                received_message = new Message(message_data);
                socket.close();
                break;
            } catch(Exception ex) {
                System.out.println("Error connecting to manager. Trying another one.");
                this.owner.switch_manager();
                i++;
            }
        }
        if(i == this.owner.get_managers().size()) {
            System.out.println("Couldn't connect to any manager.");
        }
        return received_message;
    }

    public void reclaim_request(Message message, ArrayList<PeerInfo> address_list, SaveFile file) {
        for(int i = 0; i < address_list.size(); i++) {
            String address = address_list.get(i).get_address();
            int port = address_list.get(i).get_port();
            ArrayList<byte[]> body = new ArrayList<byte[]>();
            try {
                SSLSocketFactory socketfactory = this.owner.get_context().getSocketFactory();
                SSLSocket socket = (SSLSocket) socketfactory.createSocket(address, port);
                socket.getOutputStream().write(message.build());
                byte[] data = new byte[16000];
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream stream = socket.getInputStream();
                int nRead = stream.read(data, 0, data.length);
                buffer.write(data, 0, nRead);
                byte[] message_data = buffer.toByteArray();
                String answer = new String(message_data);
            
                if(answer.equals("READY")) {
                    for(int j = 0; j < file.get_body().size(); j++) {
                        socket.getOutputStream().write(file.get_body().get(j));
                        InputStream istream = socket.getInputStream();
                        data = new byte[3];
                        istream.read(data, 0, data.length);
                        String ack = new String(data);
                        if(!ack.equals("ACK")) {
                            System.out.println("Error sending backup request.");
                            break;
                        }
                    }
                }
                System.out.println("Sent backup request to peer.");
                socket.close();
                break;
            } catch(Exception ex) {
                System.out.print("Error sending backup request to peer.");
                if(i < address_list.size()-1)
                    System.out.println(" Trying another one.");
                else System.out.println();
            }
        }
    }

    public void delete_file(String file_id) {
        int occupied = 0;
        
        File file_dir = new File("peer" + this.owner.get_id() + "/backup/" + file_id);
        if(!file_dir.exists()) {
            System.out.println("Trying to delete file. File directory doesn't exist.");
            return;
        }
        else {
            occupied = (int) file_dir.length();
            file_dir.delete();
        }
        
        this.owner.add_to_free_space(occupied);

        Message message = new Message("DELETED", this.owner.get_id(), file_id, this.owner.get_free_space(), null, null, -1, null);
        int i = 0;
        while(i < this.owner.get_managers().size()){
            try {
                SSLSocket socket = (SSLSocket) this.owner.get_context().getSocketFactory().createSocket(this.owner.get_manager_address(), 
                    this.owner.get_manager_port());
                socket.getOutputStream().write(message.build());
                break;
            } catch(Exception ex) {
                System.out.println("Error connecting to server. Trying another one.");
                this.owner.switch_manager();
                i++;
            }
        }
        if(i == this.owner.get_managers().size()) {
            System.out.println("Couldn't connect to any server.");
        }
    }
}