import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceiveMessageMC implements Runnable {
    private Peer peer;
    private byte[] message;
    public ReceiveMessageMC(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    public void run() {
        Message m = new Message(this.message);
        if(m.get_type().equals("STORED")) {
            String key = m.get_file_id() + ":" + m.get_chunk_no();
            if(this.peer.get_chunk_occurrences().get(key) != null) {
                ArrayList<Integer> senders = this.peer.get_chunk_occurrences().get(key);
                boolean found = false;
                for(int i = 0; i < senders.size(); i++) {
                    if(senders.get(i) == m.get_sender_id()) {
                        found = true;
                    }
                }
                if(!found) {
                    senders.add(m.get_sender_id());
                }
            }
            else {
                ArrayList<Integer> senders = new ArrayList<>();
                senders.add(m.get_sender_id());
                this.peer.get_chunk_occurrences().put(key, senders);
            }
        } else if(m.get_type().equals("GETCHUNK")) {
            for(int i = 0; i < this.peer.get_chunks().size(); i++) {
                if(this.peer.get_chunks().get(i).get_file_id().equals(m.get_file_id()) 
                        && this.peer.get_chunks().get(i).get_chunk_no() == m.get_chunk_no()) {
                    Message new_m = new Message("CHUNK", "1.0", this.peer.get_id(), m.get_file_id(),
                        m.get_chunk_no(), 0, this.peer.get_chunks().get(i).get_body());
                    Random rand = new Random();
                    int random_delay = rand.nextInt(401);
                    this.peer.get_thread_executor().schedule(
                        new MulticasterChunkThread(this.peer.get_mdr_address(), this.peer.get_mdr_port(), this.peer,
                            new_m.build(), m.get_file_id(), m.get_chunk_no()), 
                        random_delay, TimeUnit.MILLISECONDS);
                        break;
                }
            }
        } else if(m.get_type().equals("DELETE")){
            for(int i = 0; i < this.peer.get_chunks().size(); i++) {
                if(this.peer.get_chunks().get(i).get_file_id().equals(m.get_file_id())){
                    this.peer.get_chunks().remove(i);
                    i--;
                }
            }
        }
    }
}