import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class BackupThread implements Runnable {
    private String file_name;
    private int rep_degree;
    private Peer owner;
    private Message received_message;

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
        this.connect_to_manager();
    }

    public void connect_to_manager() {
        Message message = new Message("BACKUP", this.owner.get_id(), null, this.rep_degree, null, null, -1);
        try {
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(this.owner.get_manager_address(), this.owner.get_manager_port());
            socket.getOutputStream().write(message.build());
            System.out.println("Sent backup request to manager.");
            byte[] data = new byte[10000000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = socket.getInputStream();
            int nRead = 0;
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] message_data = buffer.toByteArray();
            this.received_message = new Message(message_data);
            System.out.println("Received answer to backup request from manager.");
            System.out.println(new String(message_data));
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error connecting to server.");
            ex.printStackTrace();
        }
    }
}