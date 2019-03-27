public class ReceiveMessageMDB implements Runnable {
    private Peer peer;
    private Message message;


    public ReceiveMessageMDB(byte[] message, Peer peer) {
        this.peer = peer;
        this.message = new Message(message);
        if(this.message.get_type().equals("PUTCHUNK")) {
            if(this.message.get_sender_id() != peer.get_id()) {
                boolean found = false;
                for(int i = 0; i < peer.get_chunks().size(); i++) {
                    if(this.message.get_file_id().equals(peer.get_chunks().get(i).get_file_id()) && this.message.get_chunk_no() 
                                                    == peer.get_chunks().get(i).get_chunk_no())
                        found = true;
                }
                if(!found) {
                    Chunk new_chunk = new Chunk(this.message.get_file_id(), this.message.get_rep_degree(), this.message.get_body(), this.message.get_chunk_no());
                    peer.get_chunks().add(new_chunk);
                }
            }
        }
    }

    public void run() {
        if(this.message.get_sender_id() != peer.get_id()) {
            String aux = " ";
            Message send_m = new Message("STORED", "1.0", peer.get_id(), this.message.get_file_id(), 
                                                    this.message.get_chunk_no(), 0, aux.getBytes());
            peer.sendMessageMC(send_m.build());
        }
    }
}