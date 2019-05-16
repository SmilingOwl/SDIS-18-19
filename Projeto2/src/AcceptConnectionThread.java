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
    private Peer owner;
    
    public AcceptConnectionThread(SSLSocket socket, ScheduledThreadPoolExecutor thread_executor, Peer owner) {
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
            int nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            byte[] message_data = buffer.toByteArray();
            System.out.println("Received message.");
            
            System.out.println(new String(message_data));
            
            this.thread_executor.execute(new MessageHandler(this.owner, this.socket, message_data));
            
            //Thread message handler for peer TODO
        } catch(Exception ex) {
            System.out.println("Error receiving message.");
            ex.printStackTrace();
        }
    }
}