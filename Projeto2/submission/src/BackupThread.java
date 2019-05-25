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
        int occupied = 0;
        for(int i = 0; i < file.get_body().size(); i++) {
            occupied += file.get_body().get(i).length;
        }
        this.owner.get_files().put(file_name, file);
        Message to_manager = new Message("BACKUP", this.owner.get_id(), null, this.rep_degree, null, null, occupied, null);
        Message manager_answer = this.backup_request_manager(to_manager);
        ArrayList<PeerInfo> address_list = manager_answer.get_peers();
        for(int i = 0; i < address_list.size(); i++) {
            Message to_peer = new Message("P2P_BACKUP", this.owner.get_id(), file.get_id(), file.get_body().size(),
                null, address_list.get(i).get_address(), address_list.get(i).get_port(), null);
            this.backup_request(to_peer, address_list.get(i).get_port(), address_list.get(i).get_address(), file);
        }
    }

    public Message backup_request_manager(Message message) {
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
                System.out.println("\nReceived message: " + new String(message_data));
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

    public void backup_request(Message message, int port, String address, SaveFile file) {
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
                for(int i = 0; i < file.get_body().size(); i++) {
                    socket.getOutputStream().write(file.get_body().get(i));
                    InputStream istream = socket.getInputStream();
                    data = new byte[3];
                    istream.read(data, 0, data.length);
                    String ack = new String(data);
                    if(!ack.equals("ACK")) {
                        System.out.println("Error sending backup request from failed ACK.");
                        break;
                    }
                }
            }
            System.out.println("Sent backup request to peer.");
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error sending backup request to peer.");
            ex.printStackTrace();
        }
    }
}