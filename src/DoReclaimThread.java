import java.io.File;
import java.util.ArrayList;

public class DoReclaimThread implements Runnable {
    private Peer peer;

    public DoReclaimThread(Peer peer) {
        this.peer = peer;
    }

    public void run() {
        int i = 0;
        while(this.peer.get_free_space() < 0) {
            if(i < this.peer.get_chunks().size()) {
                String chunk_name = this.peer.get_chunks().get(i).get_file_id() + ":" + this.peer.get_chunks().get(i).get_chunk_no();
                ArrayList<Integer> occurrences_list = this.peer.get_chunk_occurrences().get(chunk_name);
                if(occurrences_list != null) {
                    int occurrences = occurrences_list.size();
                    if(occurrences < this.peer.get_chunks().get(i).get_rep_degree()) {
                        int chunk_n = this.peer.get_chunks().get(i).get_chunk_no();
                        String file_id = this.peer.get_chunks().get(i).get_file_id();
                        int occupied = this.peer.get_chunks().get(i).get_body().length;
                        this.peer.add_to_free_space(occupied);
                        System.out.println("After removing chunk on reclaim, I have " + this.peer.get_free_space() + " available");
                        File currentFile = new File("peer" + this.peer.get_id() + "/backup/" 
                                        + this.peer.get_chunks().get(i).get_file_id() + "/chk" + this.peer.get_chunks().get(i).get_chunk_no());
                        if(!currentFile.exists()) {
                            System.out.println("Trying to delete chunk on reclaim. File doesn't exist.");
                            continue;
                        }
                        currentFile.delete();
                        Message message = new Message("REMOVED", "1.0", this.peer.get_id(), file_id, chunk_n, 0, null);
                        this.peer.sendMessageMC(message.build());
                        //CHECK IF FOLDER IS EMPTY!
                        this.peer.get_chunks().remove(i);
                        i--;
                    }
                }
            } else 
                break;
            i++;
        }
        i = 0;
        while(this.peer.get_free_space() < 0) {
            if(i < this.peer.get_chunks().size()) {
                int chunk_n = this.peer.get_chunks().get(i).get_chunk_no();
                String file_id = this.peer.get_chunks().get(i).get_file_id();
                String chunk_name = this.peer.get_chunks().get(i).get_file_id() + ":" + this.peer.get_chunks().get(i).get_chunk_no();
                int occupied = this.peer.get_chunks().get(i).get_body().length;
                this.peer.add_to_free_space(occupied);
                System.out.println("After removing chunk on reclaim, I have " + this.peer.get_free_space() + " available");
                File currentFile = new File("peer" + this.peer.get_id() + "/backup/" 
                                + this.peer.get_chunks().get(i).get_file_id() + "/chk" + this.peer.get_chunks().get(i).get_chunk_no());
                if(!currentFile.exists()) {
                    System.out.println("Trying to delete chunk on reclaim. File doesn't exist.");
                    continue;
                }
                currentFile.delete();
                Message message = new Message("REMOVED", "1.0", this.peer.get_id(), file_id, chunk_n, 0, null);
                this.peer.sendMessageMC(message.build());
                this.peer.get_chunks().remove(i);
                i--;
            }
            else break;
            i++;
        }
    }
}