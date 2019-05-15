import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

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
            SSLServerSocketFactory server_socket_factory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket server_socket =
                (SSLServerSocket) server_socket_factory.createServerSocket(this.port);
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