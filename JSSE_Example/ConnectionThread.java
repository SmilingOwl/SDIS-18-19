import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import javax.net.ssl.SSLSocket;

public class ConnectionThread implements Runnable {
    private SSLSocket sslsocket;
    
    public ConnectionThread(SSLSocket socket) {
        this.sslsocket = socket;
    }

    public void run() {
        try {
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String string = null;
            while ((string = bufferedreader.readLine()) != null) {
                System.out.println(string);
                System.out.flush();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}