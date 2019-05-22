import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SendMessage {
    public String address;
    public int port;
    public Message message;
    public SSLSocketFactory socket_factory;

    public SendMessage(String address, int port, Message message, SSLSocketFactory socket_factory) {
        this.address = address;
        this.port = port;
        this.message = message;
        this.socket_factory = socket_factory;
    }

    public void run() {
        byte[] to_send = message.build();
        try {
            SSLSocket socket = (SSLSocket) this.socket_factory.createSocket(this.address, this.port);
            socket.getOutputStream().write(to_send);
        } catch(Exception ex) {
            System.out.println("Error connecting to server.");
        }

    }
}