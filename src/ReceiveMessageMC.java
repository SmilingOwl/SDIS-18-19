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
            for(int i = 0; i < peer.get_files().size(); i++) {
                if(peer.get_files().get(i).get_id().equals(m.get_file_id())) {
                    peer.get_files().get(i).get_chunks().get(m.get_chunk_no()-1).increase_curr_rep_degree(m.get_sender_id());
                    break;
                }
            }
            for(int j = 0; j < peer.get_chunks().size(); j++) {
                if(peer.get_chunks().get(j).get_file_id().equals(m.get_file_id()) && peer.get_chunks().get(j).get_chunk_no() == m.get_chunk_no()) {
                    peer.get_chunks().get(j).increase_curr_rep_degree(m.get_sender_id());
                    break;
                }
            }
        }
    }
}