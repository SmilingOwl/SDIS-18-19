import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.net.ssl.SSLSocket;

public class AcceptConnectionThread implements Runnable {
    private SSLSocket socket;
    
    public AcceptConnectionThread(SSLSocket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            System.out.println("Connection accepted");
            byte[] data = new byte[65000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = this.socket.getInputStream();
            int nRead = 0;
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            byte[] message_data = buffer.toByteArray();
            System.out.println(new String(message_data));
        } catch(Exception ex) {
            System.out.println("Error receiving message.");
        }
    }
}