import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ReceiveMessageMDB implements Runnable {
    private Peer peer;
    private Message message;
    private boolean send_message = false;

    public ReceiveMessageMDB(byte[] message, Peer peer) {
        this.peer = peer;
        this.message = new Message(message);
    }

    public void run() {
        if(this.message.get_type().equals("PUTCHUNK")) {
            if(this.message.get_sender_id() != peer.get_id()) {
                boolean found = false;
                for(int i = 0; i < peer.get_chunks().size(); i++) {
                    if(this.message.get_file_id().equals(peer.get_chunks().get(i).get_file_id()) 
                            && this.message.get_chunk_no() == peer.get_chunks().get(i).get_chunk_no()) {
                        found = true;
                        if(this.peer.get_id() != this.message.get_sender_id()){
                            this.peer.add_chunk_not_to_send(this.message.get_file_id(), this.message.get_chunk_no());
                        }
                        break;
                    }
                }
                if(this.message.get_version().equals("2.0")) {
                    String key = this.message.get_file_id() + ":" + this.message.get_chunk_no();
                    if(this.peer.get_chunk_occurrences().get(key) != null &&
                        this.peer.get_chunk_occurrences().get(key).size() >= this.message.get_rep_degree()) {
                            found = true;
                    }
                }
                if(!found && (this.peer.get_free_space() >= this.message.get_body().length)) {
                    send_message = true;
                    Chunk new_chunk = new Chunk(this.message.get_file_id(), this.message.get_rep_degree(), 
                            this.message.get_body(), this.message.get_chunk_no());
                    String new_file_name = "peer" + this.peer.get_id() + "/backup/" + this.message.get_file_id() 
                            + "/chk" + this.message.get_chunk_no();
                    try {
                        new File("peer" + this.peer.get_id() + "/backup/" + this.message.get_file_id()).mkdirs();
                        File file = new File(new_file_name);
                        file.createNewFile(); 
                        FileOutputStream fos = new FileOutputStream(new_file_name);
                        fos.write(new_chunk.get_body());
                        fos.close();
                    } catch(IOException ex) {
                        System.out.println("Error in creating chunk files.");
                    }
                    peer.get_chunks().add(new_chunk);
                    peer.add_to_free_space(-1 * new_chunk.get_body().length);
                    String key = this.message.get_file_id() + ":" + this.message.get_chunk_no();
                    if(this.peer.get_chunk_occurrences().get(key) != null) {
                        this.peer.get_chunk_occurrences().get(key).add(this.peer.get_id());
                        this.peer.get_thread_executor().execute(new ManageDataFilesThread(this.peer));
                    }
                    else {
                        ArrayList<Integer> senders = new ArrayList<>();
                        senders.add(this.peer.get_id());
                        this.peer.get_chunk_occurrences().put(key, senders);
                        this.peer.get_thread_executor().execute(new ManageDataFilesThread(this.peer));
                    }
                }
            }
        }
        if(this.message.get_sender_id() != peer.get_id() && send_message) {
            String aux = " ";
            Message send_m = new Message("STORED", peer.get_version(), peer.get_id(), this.message.get_file_id(), 
                                                    this.message.get_chunk_no(), 0, aux.getBytes());
            peer.sendMessageMC(send_m.build());
        }
    }
}