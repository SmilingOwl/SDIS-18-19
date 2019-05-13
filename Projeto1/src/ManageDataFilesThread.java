import java.io.FileWriter;
import java.util.ArrayList;

public class ManageDataFilesThread implements Runnable {
    private Peer peer;

    public ManageDataFilesThread(Peer peer) {
        this.peer = peer;
    }

    public void run() {
        this.update_occurrences();
        this.update_files();
    }

    private void update_occurrences() {
        try {
            FileWriter fw = new FileWriter("peer" + this.peer.get_id() + "/data/occurrences.txt", false);
            for (String key : this.peer.get_chunk_occurrences().keySet()) { 
                ArrayList<Integer> occurrences = this.peer.get_chunk_occurrences().get(key);
                String to_write = key + " ";
                for(int i = 0; i < occurrences.size(); i++) {
                    to_write += occurrences.get(i);
                    if(i != occurrences.size()-1)
                        to_write += " ";
                    else
                        to_write += "\n";
                }
                fw.write(to_write);
            }
            fw.close();
        }
        catch(Exception ex) {
            System.out.println("Error updating occurences.txt");
        }
    }

    private void update_files() {
        try {
            FileWriter fw = new FileWriter("peer" + this.peer.get_id() + "/data/files.txt", false);
            for (String key : this.peer.get_files().keySet()) { 
                SaveFile file = this.peer.get_files().get(key);
                String to_write = key + " " + file.get_id() + " " + file.get_chunks().size() + " " + file.get_rep_degree();
                fw.write(to_write);
            }
            fw.close();
        }
        catch(Exception ex) {
            System.out.println("Error updating files.txt");
        }
    }
}