import javax.net.ssl.SSLSocket;
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
        System.out.println("Received message: " + new String(msg));
    }

    public void run() {
        if (this.message.get_type().equals("P2P_BACKUP")) {
            this.backup_request();
        } else if (this.message.get_type().equals("P2P_RESTORE")) {
            this.restore_request();
        } else if(this.message.get_type().equals("DELETE")){
            this.delete_request();
        }
    }

    /*************** Message Handler Functions ***************/
    public void backup_request() {
        System.out.println("\nReceived backup request.");
        String file_id = this.message.get_file_id();
        int num_chunks = this.message.get_rep_degree();
        ArrayList<byte[]> body = new ArrayList<byte[]>();
        String ready_msg = "READY";
        String ack_msg = "ACK";
        int n = 0;
        try {
            socket.getOutputStream().write(ready_msg.getBytes());
            byte[] data = new byte[16000];
            InputStream stream = socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            while (nRead > 0) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                buffer.write(data, 0, nRead);
                byte[] message_data = buffer.toByteArray();
                body.add(message_data);
                socket.getOutputStream().write(ack_msg.getBytes());
                if(n == num_chunks-1)
                    break;
                nRead = stream.read(data, 0, data.length);
                n++;
            }
            socket.close();
        } catch (Exception ex) {
            System.out.println("Error writing to socket.");
        }
        new SaveFile(this.owner.get_id(), file_id, "backup", body);
        Message message = new Message("STORED", this.owner.get_id(), file_id, -1, null, null, -1, null);
        SendMessage send_stored = new SendMessage(this.owner.get_manager_address(), this.owner.get_manager_port(),
                message, this.owner.get_context().getSocketFactory());
        send_stored.run();
    }

    public void restore_request() {
        System.out.println("\nReceived restore request.");
        String file_id = this.message.get_file_id();
        SaveFile file_to_send = new SaveFile("peer" + this.owner.get_id() + "/backup/" + file_id, 0);
        Message file_message = new Message("FILE", this.owner.get_id(), file_id, file_to_send.get_body().size(), null,
                null, -1, null);
        try {
            if(file_to_send.get_error()) {
                String error_message = "ERR";
                socket.getOutputStream().write(error_message.getBytes());
                return;
            }
            socket.getOutputStream().write(file_message.build());
            System.out.println("Sent file message");
            for (int i = 0; i < file_to_send.get_body().size(); i++) {
                socket.getOutputStream().write(file_to_send.get_body().get(i));
                InputStream istream = socket.getInputStream();
                byte[] data = new byte[3];
                istream.read(data, 0, data.length);
                String ack = new String(data);
                if(!ack.equals("ACK")) {
                    System.out.println("Error sending file on restore protocol.");
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error writing to socket.");
        }
    }

    public void delete_request() {
        System.out.println("\nReceived restore request.");
        String file_id = this.message.get_file_id();
                
        try {
            this.owner.get_files().remove(file_id);
            System.out.println("deleted file");

        } catch (Exception ex) {
            System.out.println("Error writing to socket.");

        }
        Message message = new Message("DELETED", this.owner.get_id(), file_id, -1, null, null, -1, null);
        SendMessage send_deleted = new SendMessage(this.owner.get_manager_address(), this.owner.get_manager_port(), message,
            this.owner.get_context().getSocketFactory());
        send_deleted.run();
    }
}