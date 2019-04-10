import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DoRestoreThread implements Runnable {
    private Peer peer;
    private int number_of_chunks;
    private int svc_port;
    private String file_id;

    public DoRestoreThread(Peer peer, int number_of_chunks, int svc_port, String file_id) {
        this.peer = peer;
        this.svc_port = svc_port;
        this.file_id = file_id;
    }

    public void run() {
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
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String chunk = in.readLine();

                } catch(Exception ex) {
                    System.out.println("ERROR TCP: on SeerverSocket");
                }
            }
        }
    }
}