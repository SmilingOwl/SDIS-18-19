import java.io.FileInputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

public class ConnectionThread implements Runnable {
    private int port;
    private ScheduledThreadPoolExecutor thread_executor;
    private Object owner;
    
    public ConnectionThread(int port, ScheduledThreadPoolExecutor thread_executor, Object owner) {
        this.port = port;
        this.thread_executor = thread_executor;
        this.owner = owner;
    }

    public void run() {
        try{
            SSLContext context = SSLContext.getInstance("TLS");
            KeyManagerFactory key_manager_factory = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory trust_manager_factory = TrustManagerFactory.getInstance("SunX509");
            char[] passphrase = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore ks2 = KeyStore.getInstance("JKS");
      
            ks.load(new FileInputStream("keystore.jks"), passphrase);
            ks2.load(new FileInputStream("truststore.ts"), passphrase);
            trust_manager_factory.init(ks2);
            key_manager_factory.init(ks, passphrase);
            context.init(key_manager_factory.getKeyManagers(), trust_manager_factory.getTrustManagers(), null);

            SSLServerSocketFactory server_socket_factory =
                (SSLServerSocketFactory) context.getServerSocketFactory();
            SSLServerSocket server_socket =
                (SSLServerSocket) server_socket_factory.createServerSocket(this.port);
            server_socket.setNeedClientAuth(true);
            System.out.println("Ready to accept connection requests.");
            while (true) {
                SSLSocket socket = (SSLSocket) server_socket.accept();
                this.thread_executor.execute(new AcceptConnectionThread(socket, this.thread_executor, this.owner));
            }
        } catch (Exception ex) {
            System.out.println("Error creating server socket.");
        }
    }
}