public class ReceiveMessageMDB implements Runnable {
    private Peer peer;
    private Message message;


    public ReceiveMessageMDB(byte[] message, Peer peer) {
        this.peer = peer;
        this.message = new Message(message);
        if(m.get_type().equals("PUTCHUNK")) {
            boolean found_file =  false;
            if(m.get_sender_id() != this.id) {
                boolean found = false;
                for(int i = 0; i < this.myChunks.size(); i++) {
                    if(m.get_file_id().equals(peer.get_chunks().get(i).get_file_id()) && m.get_chunk_no() 
                                                    == peer.get_chunks().get(i).get_chunk_no())
                        found = true;
                }
                if(!found) {
                    Chunk new_chunk = new Chunk(m.get_file_id(), m.get_rep_degree(), m.get_body(), m.get_chunk_no());
                    peer.get_chunks().add(new_chunk);
                }
            }
        }
    }

    public void run() {
        Message m = new Message(this.message);
        if(m.get_type().equals("PUTCHUNK")) {
            boolean found_file =  false;
            if(m.get_sender_id() != this.id) {
                boolean found = false;
                for(int i = 0; i < this.myChunks.size(); i++) {
                    if(m.get_file_id().equals(peer.get_chunks().get(i).get_file_id()) && m.get_chunk_no() 
                                                    == peer.get_chunks().get(i).get_chunk_no())
                        found = true;
                }
                if(!found) {
                    Chunk new_chunk = new Chunk(m.get_file_id(), m.get_rep_degree(), m.get_body(), m.get_chunk_no());
                    peer.get_chunks().add(new_chunk);
                }
                String aux = " ";
                Message send_m = new Message("STORED", "1.0", peer.get_id(), m.get_file_id(), m.get_chunk_no(), 0, aux.getBytes());
                Random rand = new Random();
                int random_delay = rand.nextInt(401);
                peer.sendMessageMC(send_m.build(), random_delay);
            }
        }
    }
}