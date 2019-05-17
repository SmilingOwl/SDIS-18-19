import javax.net.ssl.SSLSocket;
import java.net.*;
import java.io.*;
import java.util.*;

public class PeerMessageHandler implements Runnable {
    private Message message;
    private Peer owner;
    private SSLSocket socket;
    
    public PeerMessageHandler(Peer owner, SSLSocket socket, byte[] msg) {
        this.owner = owner;
        this.message = new Message(msg);
        this.socket = socket;
    }

    public void run() {
        if (this.message.get_type().equals("P2P_BACKUP")) {
            this.backup_request();
        }
    }

    public void backup_request() {
        System.out.println("Received backup request.");
        String file_id = this.message.get_file_id();
        int num_chunks = this.message.get_rep_degree();
        ArrayList<byte[]> body = new ArrayList<byte[]>();
        String ready_msg = "READY";
        int n = 0;
        try {
            socket.getOutputStream().write(ready_msg.getBytes());
            byte[] data = new byte[16000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            while(n < num_chunks && nRead > 0) {
                buffer.write(data, 0, nRead);
                byte[] message_data = buffer.toByteArray();
                body.add(message_data);
                nRead = stream.read(data, 0, data.length);
            }
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
        SaveFile write = new SaveFile(this.owner.get_id(), file_id, body);
        Message message = new Message("STORED", this.owner.get_id(), file_id, -1, null, null, -1, null);
        SendMessage send_stored = new SendMessage(this.owner.get_manager_address(), this.owner.get_manager_port(), message);
        send_stored.run();
    }
}