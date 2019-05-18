import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class BackupThread implements Runnable {
    private String file_name;
    private int rep_degree;
    private Peer owner;

    public BackupThread(String file_name, int rep_degree, Peer owner) {
        this.file_name = file_name;
        this.rep_degree = rep_degree;
        this.owner = owner;
    }

    public void run() {
        SaveFile file = new SaveFile(file_name, rep_degree);
        this.owner.get_files().put(file_name, file);
        //TEST, to use to restore a file:
        //SaveFile write = new SaveFile(file_name, file.get_body());
        Message to_manager = new Message("BACKUP", this.owner.get_id(), null, this.rep_degree, null, null, -1, null);
        Message manager_answer = this.backup_request_manager(to_manager, this.owner.get_manager_port(), 
            this.owner.get_manager_address());
        ArrayList<PeerInfo> address_list = manager_answer.get_peers();
        for(int i = 0; i < address_list.size(); i++) {
            Message to_peer = new Message("P2P_BACKUP", this.owner.get_id(), file.get_id(), file.get_body().size(),
                null, address_list.get(i).get_address(), address_list.get(i).get_port(), null);
            this.backup_request(to_peer, address_list.get(i).get_port(), address_list.get(i).get_address(), file);
        }
    }

    public Message backup_request_manager(Message message, int port, String address) {
        Message received_message = null;
        try {
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(address, port);
            socket.getOutputStream().write(message.build());
            System.out.println("Sent backup request.");
            byte[] data = new byte[16000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            byte[] message_data = buffer.toByteArray();
            received_message = new Message(message_data);
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error connecting to server.");
        }
        return received_message;
    }

    public Message backup_request(Message message, int port, String address, SaveFile file) {
        Message received_message = null;
        try {
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
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
                for(int i = 0; i < file.get_body().size(); i++) {
                    socket.getOutputStream().write(file.get_body().get(i));
                }
            }
            System.out.println("Sent backup request.");
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error connecting to server.");
            ex.printStackTrace(); //TODO delete
        }
        return received_message;
    }
}