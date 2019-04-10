import java.io.File;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileOutputStream;


public class SaveFile {
    private String id;
    private int rep_degree;
    private File file;
    private String file_name;
    private int number_of_chunks;
    private ArrayList<Chunk> chunks;
    private Peer peer;

    SaveFile(String file_path, int rep_degree) {
        this.file = new File(file_path);
        this.rep_degree = rep_degree;
        String unhashed_id = file.getName() + file.getParent() + file.lastModified();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded_hash = digest.digest(unhashed_id.getBytes(StandardCharsets.UTF_8));
            StringBuffer hex_string = new StringBuffer();
            for (int i = 0; i < encoded_hash.length; i++) {
                String hex = Integer.toHexString(0xff & encoded_hash[i]);
                if(hex.length() == 1) hex_string.append('0');
                    hex_string.append(hex);
            }
            this.id = hex_string.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }

        this.chunks = new ArrayList<>();
        
        byte[] buffer = new byte[Chunk.MAX_SIZE];
        int chunk_counter = 0;
        try {
            FileInputStream file_is = new FileInputStream(this.file);
            BufferedInputStream buffered_is = new BufferedInputStream(file_is);
            int num_buf = buffered_is.read(buffer);
            while(num_buf > 0) {
                chunk_counter++;
                byte[] buf = Arrays.copyOfRange(buffer, 0, num_buf);
                Chunk new_chunk = new Chunk(this.id, this.rep_degree, buf, chunk_counter);
                this.chunks.add(new_chunk);
                buffer = new byte[Chunk.MAX_SIZE];
                num_buf = buffered_is.read(buffer);
            }
            file_is.close();
            buffered_is.close();
        } catch(IOException ex) {
            System.out.println("Error splitting file");
            ex.printStackTrace();
        }
        number_of_chunks = this.chunks.size();
    }

    SaveFile(String file_name, int number_of_chunks, Peer peer) {
        this.file_name = file_name;
        this.number_of_chunks = number_of_chunks;
        this.chunks = new ArrayList<Chunk>();
        this.peer = peer;
        this.id = this.peer.get_files().get(this.file_name).get_id();
        try {
            File file = new File("peer" + this.peer.get_id() + "/restored/" + this.file_name);
            file.createNewFile();            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add_chunk(byte[] chunk, int chunk_no) {
        Chunk new_chunk = new Chunk(this.id, chunk, chunk_no);
        this.chunks.add(new_chunk);
        if(this.chunks.size() == this.number_of_chunks) 
            this.add_to_file();
    }

    public void add_to_file() {
        Collections.sort(chunks);
        try {
            FileOutputStream fos = new FileOutputStream("peer" + this.peer.get_id() + "/restored/" + this.file_name);
            for(int i = 0; i < chunks.size(); i++) {
                fos.write(chunks.get(i).get_body());
            }
            fos.close();
            if(this.peer.get_myFilesToRestore().get(this.id) != null)
                this.peer.get_myFilesToRestore().remove(this.id);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<Chunk> get_chunks() {
        return this.chunks;
    }

    public String get_id() {
        return this.id;
    }

    public File get_file(){
        return this.file;
    }

    public int get_rep_degree(){
        return this.rep_degree;
    }
}
