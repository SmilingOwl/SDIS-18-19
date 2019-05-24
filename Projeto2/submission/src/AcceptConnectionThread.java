import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import javax.net.ssl.SSLSocket;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class AcceptConnectionThread implements Runnable {
    private SSLSocket socket;
    private ScheduledThreadPoolExecutor thread_executor;
    private Object owner;
    
    public AcceptConnectionThread(SSLSocket socket, ScheduledThreadPoolExecutor thread_executor, Object owner) {
        this.socket = socket;
        this.thread_executor = thread_executor;
        this.owner = owner;
    }

    public void run() {
        try {
            System.out.println("\nConnection accepted");
            byte[] data = new byte[16000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = this.socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            byte[] message_data = buffer.toByteArray();
            
            if(this.owner instanceof PeerManager) {
                PeerManager peer_manager = (PeerManager) this.owner;
                this.thread_executor.execute(new ManagerMessageHandler(peer_manager, this.socket, message_data));
            } else if(this.owner instanceof Peer) {
                Peer peer = (Peer) this.owner;
                this.thread_executor.execute(new PeerMessageHandler(peer, this.socket, message_data));
            }
        } catch(Exception ex) {
            System.out.println("Error receiving message.");
        }
    }
}