import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class DoRestoreThread implements Runnable {
    private Peer peer;
    private int number_of_chunks;
    private int svc_port;
    private String file_id;

    public DoRestoreThread(Peer peer, int number_of_chunks, int svc_port, String file_id) {
        this.peer = peer;
        this.number_of_chunks = number_of_chunks;
        this.svc_port = svc_port;
        this.file_id = file_id;
    }

    public void run() {
        System.out.println("On DoRestoreThread");
        for(int i = 0; i < this.number_of_chunks; i++) {
            if(this.peer.get_version().equals("1.0")) {
                Message to_send = new Message("GETCHUNK", "1.0", this.peer.get_id(), file_id, i+1, 0, null);
                this.peer.sendMessageMC(to_send.build());
            }
            else if(this.peer.get_version().equals("2.0")) {
                String svc_address = null;
                try {
                    svc_address = InetAddress.getLocalHost().getHostAddress();
                }
                catch (Exception ex) {
                    System.out.println("ERROR local host");
                }
                Message to_send = new Message("GETCHUNK", "2.0", this.peer.get_id(), file_id, i+1, 0, null, svc_address, this.svc_port);
                this.peer.sendMessageMC(to_send.build());
                try {
                    ServerSocket svc_socket = new ServerSocket(this.svc_port);
                    Socket socket = svc_socket.accept();
                    byte[] data = new byte[65000];
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    InputStream stream = socket.getInputStream();
                    int nRead = 0;
                    while ((nRead = stream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    byte[] message_data = buffer.toByteArray();
                    // System.out.println("available: " + stream.available());
                    // stream.read(data);
                    svc_socket.close();
                    socket.close();
                    Message message = new Message(message_data);
                    if(message.get_type().equals("CHUNK")) {
                        if(this.peer.get_myFilesToRestore().get(message.get_file_id()) != null) {
                            SaveFile file_to_save = this.peer.get_myFilesToRestore().get(message.get_file_id());
                            file_to_save.add_chunk(message.get_body(), message.get_chunk_no());
                        } else if(this.peer.get_id() != message.get_sender_id()){
                            this.peer.add_chunk_not_to_send(message.get_file_id(), message.get_chunk_no());
                        }
                    }
                } catch(Exception ex) {
                    System.out.println("ERROR TCP: on ServerSocket");
                    ex.printStackTrace();
                }
            }
        }
    }
}