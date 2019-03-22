public class ReceiveMessageMDR implements Runnable {
    private Peer peer;
    private Message message;


    public ReceiveMessageMDR(byte[] message, Peer peer) {
        this.peer = peer;
        this.message = new Message(message);
    }

    public void run() {
        
    }
}