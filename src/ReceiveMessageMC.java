import java.util.ArrayList;

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
                if(!senders.contains(m.get_sender_id())) {
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
                    //has chunk
                }
            }
        }
    }
}