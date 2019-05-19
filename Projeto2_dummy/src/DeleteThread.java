import java.net.*;
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

        Message to_manager = new Message("DELETE", this.owner.get_id(), file_id, -1, null, null, -1, null);
        
        Message manager_answer = this.delete_request_manager(to_manager, this.owner.get_manager_port(), 
            this.owner.get_manager_address());

        ArrayList<PeerInfo> address_list = manager_answer.get_peers();
       
        Message to_peer = new Message("DELETE", this.owner.get_id(), file_id, -1, null, null, -1, null);
       
        this.delete_request(to_peer, address_list);
    }

    public Message delete_request_manager(Message message, int port, String address) {
        Message received_message = null;
        try {
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(address, port);
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
        } catch(Exception ex) {
            System.out.println("Error connecting to manager.");
        }
        return received_message;
    }

    public void delete_request(Message message, ArrayList<PeerInfo> address_list) {
        String address = address_list.get(0).get_address();
        int port = address_list.get(0).get_port();
  
        try {
            /******** create socket ********/
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(address, port);
            
            /******** write delete message ********/
            socket.getOutputStream().write(message.build());

            
            socket.close();
            System.out.println("Sent delete request to peer.");
        } catch(Exception ex) {
            System.out.println("Error connecting to peer.");
        }
        
    }
}