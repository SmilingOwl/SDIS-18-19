import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

public class SendDeleteThread implements Runnable {
    private ConcurrentHashMap<String, ArrayList<Integer>> files;
    Peer peer;

    public SendDeleteThread(Peer peer, ConcurrentHashMap<String, ArrayList<Integer>> files) {
        this.peer = peer;
        this.files = files;
    }

    public void run() {
        while(true) {
            if(!files.isEmpty()) {
                for(String file_id : this.files.keySet()) {
                    Message to_send = new Message("DELETE", "2.0", this.peer.get_id(), file_id, 0, 0, null);
                    this.peer.sendMessageMC(to_send.build());
                }
            }
            try {
                Thread.sleep(30000);
            } catch (Exception ex) {
                System.out.println("Error in sleep.");
            }
        }
    }
}