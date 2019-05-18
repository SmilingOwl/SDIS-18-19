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
        } else if (this.message.get_type().equals("P2P_RESTORE")) {
            this.restore_request();
        }
    }

    /*************** Message Handler Functions ***************/
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
            InputStream stream = socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            while(nRead > 0) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                buffer.write(data, 0, nRead);
                byte[] message_data = buffer.toByteArray();
                body.add(message_data);
                if(n == num_chunks-1)
                    break;
                nRead = stream.read(data, 0, data.length);
                n++;
            }
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
        SaveFile write = new SaveFile(this.owner.get_id(), file_id, "backup", body);
        Message message = new Message("STORED", this.owner.get_id(), file_id, -1, null, null, -1, null);
        SendMessage send_stored = new SendMessage(this.owner.get_manager_address(), this.owner.get_manager_port(), message);
        send_stored.run();
    }

    public void restore_request() {
        System.out.println("Received restore request.");
        String file_id = this.message.get_file_id();
        SaveFile file_to_send = new SaveFile("peer" + this.owner.get_id() + "/backup/" + file_id, 0);
        Message file_message = new Message("FILE", this.owner.get_id(), file_id, file_to_send.get_body().size(), null, null, -1, null);
        try {
            socket.getOutputStream().write(file_message.build());
            System.out.println("Sent file message");
            for(int i = 0; i < file_to_send.get_body().size(); i++) {
                socket.getOutputStream().write(file_to_send.get_body().get(i));
            }
        } catch(Exception ex) {
            System.out.println("Error writing to socket.");
        }
    }
}