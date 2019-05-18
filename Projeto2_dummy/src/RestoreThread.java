import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class RestoreThread implements Runnable {
    private String file_name;
    private int rep_degree;
    private Peer owner;

    public RestoreThread(String file_name, Peer owner) {
        this.file_name = file_name;
        this.owner = owner;
    }

    public void run() {
        String file_id = SaveFile.generate_id(file_name);
        Message to_manager = new Message("RESTORE", this.owner.get_id(), file_id, -1, null, null, -1, null);
        Message manager_answer = this.restore_request_manager(to_manager, this.owner.get_manager_port(), 
            this.owner.get_manager_address());
        ArrayList<PeerInfo> address_list = manager_answer.get_peers();
        Message to_peer = new Message("P2P_RESTORE", this.owner.get_id(), file_id, -1, null, null, -1, null);
        this.restore_request(to_peer, address_list);
    }

    public Message restore_request_manager(Message message, int port, String address) {
        Message received_message = null;
        try {
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(address, port);
            socket.getOutputStream().write(message.build());
            System.out.println("Sent restore request to manager.");
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

    public void restore_request(Message message, ArrayList<PeerInfo> address_list) {
        String address = address_list.get(0).get_address();
        int port = address_list.get(0).get_port();
        ArrayList<byte[]> body = new ArrayList<byte[]>();
        try {
            /******** create socket ********/
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(address, port);
            
            /******** write restore message ********/
            socket.getOutputStream().write(message.build());

            /******** receive answer ********/
            byte[] data = new byte[16000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            byte[] message_data = buffer.toByteArray();
            Message answer = new Message(message_data);
            
            /******** receive file ********/
            if(answer.get_type().equals("FILE")) {
                int num_chunks = answer.get_rep_degree();
                int n = 0;
                nRead = stream.read(data, 0, data.length);
                while(nRead > 0) {
                    buffer = new ByteArrayOutputStream();
                    buffer.write(data, 0, nRead);
                    message_data = buffer.toByteArray();
                    body.add(message_data);
                    if(n == num_chunks - 1)
                        break;
                    nRead = stream.read(data, 0, data.length);
                    n++;
                }
            }
            socket.close();
            System.out.println("Sent restore request to peer.");
        } catch(Exception ex) {
            System.out.println("Error connecting to peer.");
        }
        SaveFile write = new SaveFile(this.owner.get_id(), this.file_name, "restore", body);
    }
}