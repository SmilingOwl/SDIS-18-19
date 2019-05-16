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
        SaveFile write = new SaveFile(this.owner.get_id(), file_id, message.get_body());
        Message message = new Message("STORED", -1, file_id, -1, null, null, -1, null);
        SendMessage send_stored = new SendMessage(this.owner.get_manager_address(), this.owner.get_manager_port(), message);
        send_stored.run();
    }
}