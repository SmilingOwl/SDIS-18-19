import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.io.File;

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
                    this.peer.get_thread_executor().execute(new ManageDataFilesThread(this.peer));
                }
            }
            else {
                ArrayList<Integer> senders = new ArrayList<>();
                senders.add(m.get_sender_id());
                this.peer.get_chunk_occurrences().put(key, senders);
                this.peer.get_thread_executor().execute(new ManageDataFilesThread(this.peer));
            }
        } else if(m.get_type().equals("GETCHUNK")) {
            for(int i = 0; i < this.peer.get_chunks().size(); i++) {
                if(this.peer.get_chunks().get(i).get_file_id().equals(m.get_file_id()) 
                        && this.peer.get_chunks().get(i).get_chunk_no() == m.get_chunk_no()) {
                    Message new_m = new Message("CHUNK", m.get_version(), this.peer.get_id(), m.get_file_id(),
                        m.get_chunk_no(), 0, this.peer.get_chunks().get(i).get_body());
                    Random rand = new Random();
                    int random_delay = rand.nextInt(401);
                    this.peer.get_thread_executor().schedule(
                        new MulticasterChunkThread(this.peer.get_mdr_address(), this.peer.get_mdr_port(), this.peer,
                            new_m.build(), m.get_file_id(), m.get_chunk_no(), m.get_port(), m.get_address(), m.get_version()),
                        random_delay, TimeUnit.MILLISECONDS);
                        break;
                }
            }
        } else if(m.get_type().equals("SENTCHUNK")) {
            if(this.peer.get_myFilesToRestore().get(m.get_file_id()) == null && 
                    this.peer.get_id() != m.get_sender_id()){
                this.peer.add_chunk_not_to_send(m.get_file_id(), m.get_chunk_no());
            }
        } else if(m.get_type().equals("DELETE")){
            boolean found_chunk = false;
            int occupied = 0;
            for(int i = 0; i < this.peer.get_chunks().size(); i++) {
                if(this.peer.get_chunks().get(i).get_file_id().equals(m.get_file_id())){
                    occupied = this.peer.get_chunks().get(i).get_body().length;
                    this.peer.get_chunks().remove(i);
                    this.peer.add_to_free_space(occupied);
                    found_chunk = true;
                    i--;
                }
            }
            if(!found_chunk)
                return;
            File file_dir = new File("peer" + this.peer.get_id() + "/backup/" + m.get_file_id());
            if(!file_dir.exists()) {
                System.out.println("Trying to delete file chunks. File directory doesn't exist.");
                return;
            }
            String[] chunks = file_dir.list();
            for(String chunk_name: chunks){
                File currentFile = new File("peer" + this.peer.get_id() + "/backup/" + m.get_file_id() + "/" + chunk_name);
                currentFile.delete();
            }
            file_dir.delete();
            for (String key : this.peer.get_chunk_occurrences().keySet()) {
                String[] ids = key.split(":");
                if(ids[0].equals(m.get_file_id())) {
                    this.peer.get_chunk_occurrences().remove(key);
                }
            }
            this.get_thread_executor().execute(new ManageDataFilesThread(this.peer));
        } else if(m.get_type().equals("REMOVED")){
            if(m.get_sender_id() != this.peer.get_id()) {
                String chunk_name = m.get_file_id() + ":" + m.get_chunk_no();
                if(this.peer.get_chunk_occurrences().get(chunk_name) != null) {
                    ArrayList<Integer> occurrences = this.peer.get_chunk_occurrences().get(chunk_name);
                    for(int i = 0; i < occurrences.size(); i++) {
                        if(occurrences.get(i) == m.get_sender_id())
                        {
                            occurrences.remove(i);
                            break;
                        }
                    }
                    this.peer.get_thread_executor().execute(new ManageDataFilesThread(this.peer));
                    for(int j = 0; j < this.peer.get_chunks().size(); j++) {
                        if(this.peer.get_chunks().get(j).get_file_id().equals(m.get_file_id())
                            && this.peer.get_chunks().get(j).get_chunk_no() == m.get_chunk_no()) {
                            Chunk chunk = this.peer.get_chunks().get(j);
                            Message message = new Message("PUTCHUNK", this.peer.get_version(), this.peer.get_id(), chunk.get_file_id(), 
                                chunk.get_chunk_no(), chunk.get_rep_degree(), chunk.get_body());
                            Random rand = new Random();
                            int random_delay = rand.nextInt(401);
                            this.peer.get_thread_executor().schedule(
                                new MulticasterPutChunkThread(this.peer.get_mdb_address(), this.peer.get_mdb_port(), 
                                    message.build(), chunk, this.peer),
                                random_delay, TimeUnit.MILLISECONDS);
                            break;
                        }
                    }
                }
            }
        }
    }
}