public class ReceiveMessageMDR implements Runnable {
    private Peer peer;
    private Message message;


    public ReceiveMessageMDR(byte[] message, Peer peer) {
        this.peer = peer;
        this.message = new Message(message);
    }

    public void run() {
        if(this.message.get_type().equals("CHUNK")) {
            if(this.peer.get_myFilesToRestore().get(this.message.get_file_id()) != null) {
                SaveFile file_to_save = this.peer.get_myFilesToRestore().get(this.message.get_file_id());
                file_to_save.add_chunk(this.message.get_body(), this.message.get_chunk_no());
            } else if(this.peer.get_id() != this.message.get_sender_id()){
                this.peer.add_chunk_not_to_send(this.message.get_file_id(), this.message.get_chunk_no());
            }
        }
    }
}