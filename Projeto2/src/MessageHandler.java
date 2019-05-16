import javax.net.ssl.SSLSocket;
import java.net.*;
import java.io.*;
import java.util.*;
import java.math.BigInteger;

public class MessageHandler implements Runnable {
    private Message message;
    private Peer owner;
    private SSLSocket socket;
    
    public MessageHandler(Peer owner, SSLSocket socket, byte[] msg) {
        this.owner = owner;
        this.message = new Message(msg);
        this.socket = socket;
    }

    public void run() {
        System.out.println("Entered MessageHandler");
        if (this.message.get_type().equals("JOIN")) {
            this.peer_join();
        } 
    }

    public void peer_join() {
        if(!this.message.get_sender_id().equals("null")) {
            BigInteger sender_id = new BigInteger(this.message.get_sender_id());
            if(sender_id == this.owner.get_id()) {
                return;
            }
        }
        BigInteger peer_id = new BigInteger(this.message.get_peer_id());
        int port = this.message.get_port();
        String address = this.message.get_address();
        
        this.owner.get_finger_table().update_fingers(new PeerInfo(peer_id, port, address));
        this.owner.get_finger_table().print_finger_table();
    }
}