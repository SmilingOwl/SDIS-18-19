import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SendMessage {
    public String address;
    public int port;
    public Message message;

    public SendMessage(String address, int port, Message message) {
        this.address = address;
        this.port = port;
        this.message = message;
    }

    public void run() {
        byte[] to_send = message.build();
        try {
            SSLSocketFactory socketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(this.address, this.port);
            socket.getOutputStream().write(to_send);
            //TODO receive ACK, perhaps, and implement timeout, if it doesn't receive ACK in timeout, sends again x3?
            //Failure Tolerance - if error x3 and communicating with manager try another manager
        } catch(Exception ex) {
            System.out.println("Error connecting to server.");
            ex.printStackTrace();
        }

    }
}