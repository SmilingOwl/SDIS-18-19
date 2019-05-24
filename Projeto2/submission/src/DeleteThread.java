import java.io.*;
import java.util.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class DeleteThread implements Runnable {
    private String file_name;
    private Peer owner;

    public DeleteThread(String file_name, Peer owner) {
        this.file_name = file_name;
        this.owner = owner;
    }

    public void run() {
        String file_id = SaveFile.generate_id(file_name);

        Message peer_to_manager = new Message("DELETE", this.owner.get_id(), file_id, -1, null, null, -1, null);
        
        Message manager_answer = this.delete_request_manager(peer_to_manager);

        //AVAILABLE MESSAGE
        ArrayList<PeerInfo> address_list = manager_answer.get_peers();
        
        //sending delete message to all available peers
        for(int i = 0; i < address_list.size(); i++) {
            Message to_peers = new Message("DELETE", this.owner.get_id(), file_id, -1, null, null, -1, null);
            this.delete_request(to_peers, address_list.get(i).get_port(), address_list.get(i).get_address());
        }
        
    }

    public Message delete_request_manager(Message message) {
        Message received_message = null;
        int i = 0;
        while(i < this.owner.get_managers().size()){
            try {
                SSLSocketFactory socketfactory = this.owner.get_context().getSocketFactory();
                SSLSocket socket = (SSLSocket) socketfactory.createSocket(this.owner.get_manager_address(), this.owner.get_manager_port());
                socket.getOutputStream().write(message.build());
                
                System.out.println("Sent delete request to manager.");
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

    public void delete_request(Message message, int port, String address) {
  
        try {
            /******** create socket ********/
            SSLSocketFactory socketfactory = this.owner.get_context().getSocketFactory();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(address, port);
            socket.getOutputStream().write(message.build());
            System.out.println("Sent delete request to peer.");
        } catch(Exception ex) {
            System.out.println("Error connecting to peer.");
        }
        
    }
}