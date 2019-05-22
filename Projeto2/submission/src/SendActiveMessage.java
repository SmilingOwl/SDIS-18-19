import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SendActiveMessage implements Runnable {
    private Peer owner;
    
    public SendActiveMessage(Peer owner) {
        this.owner = owner;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(60000);
            } catch (Exception ex) {
                System.out.println("Error on thread sleep.");
            }
            Message active_message = new Message("ACTIVE", this.owner.get_id(), null, -1, null, null, -1, null);
            int i = 0;
            while(i < this.owner.get_managers().size()){
                try {
                    SSLSocketFactory socket_factory = this.owner.get_context().getSocketFactory();
                    SSLSocket socket = (SSLSocket) socket_factory.createSocket(this.owner.get_manager_address(), this.owner.get_manager_port());
                    socket.getOutputStream().write(active_message.build());
                    break;
                } catch(Exception ex) {
                    System.out.println("Error connecting to manager. Trying another one.");
                    this.owner.switch_manager();
                    i++;
                }
            }
            if(i == this.owner.get_managers().size()) {
                System.out.println("Couldn't connect to any manager to send active message.");
            }
        }
    }
}