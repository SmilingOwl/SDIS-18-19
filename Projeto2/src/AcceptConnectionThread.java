import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            System.out.println("Connection accepted");
            byte[] data = new byte[10000000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = this.socket.getInputStream();
            System.out.println(" - before read.");
            int nRead = stream.read(data, 0, data.length);
            System.out.println(" - after read.");
            buffer.write(data, 0, nRead);
            byte[] message_data = buffer.toByteArray();
            System.out.println("Received message.");
            
            if(this.owner instanceof PeerManager) {
                PeerManager peer_manager = (PeerManager) this.owner;
                this.thread_executor.execute(new ManagerMessageHandler(peer_manager, this.socket, message_data));
            } else if(this.owner instanceof Peer) {
                System.out.println("Inside Peer.");
                Peer peer = (Peer) this.owner;
                System.out.println("Received message.");
                System.out.println(new String(message_data));
                //Thread message handler for peer TODO
            }
        } catch(Exception ex) {
            System.out.println("Error receiving message.");
        }
    }
}