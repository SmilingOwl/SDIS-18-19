import java.util.ArrayList;

public class CheckActiveThread implements Runnable {
    private PeerManager owner;
    
    public CheckActiveThread(PeerManager owner) {
        this.owner = owner;
    }

    public void run() {
        while (true) {
            for(Integer peer_id : this.owner.get_peers().keySet()) {
                long time = System.currentTimeMillis();
                if(time - this.owner.get_peers().get(peer_id).get_time() > 90000) {
                    System.out.println("Deleting inactive peer: " + peer_id);
                    this.owner.get_peers().remove(peer_id);
                    for(String file_id : this.owner.get_files().keySet()) {
                        ArrayList<Integer> peers = this.owner.get_files().get(file_id);
                        for(int i = 0; i < peers.size(); i++) {
                            if(peers.get(i) == peer_id) {
                                peers.remove(i);
                                break;
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(120000);
            } catch(Exception ex) {
                System.out.println("Error on sleep.");
            }
        }
    }
}