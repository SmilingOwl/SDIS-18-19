import java.io.File;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;


public class SaveFile {
    private String id;
    private int rep_degree;
    private File file;
    ArrayList<Chunk> chunks;

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
        //System.out.println("generated id: " + this.id);

        this.chunks = new ArrayList<>();
        
        byte[] buffer = new byte[Chunk.MAX_SIZE];
        int chunk_counter = 0;
        try {
            FileInputStream file_is = new FileInputStream(this.file);
            BufferedInputStream buffered_is = new BufferedInputStream(file_is);
            while(buffered_is.read(buffer) > 0) {
                chunk_counter++;
                Chunk new_chunk = new Chunk(this.id, this.rep_degree, buffer, chunk_counter);
                this.chunks.add(new_chunk);
                buffer = new byte[Chunk.MAX_SIZE];
            }
            file_is.close();
            buffered_is.close();
        } catch(IOException ex) {
            System.out.println("Error splitting file");
            ex.printStackTrace();
        }
        //System.out.println("Number of chunks: " + this.chunks.size());
    }
}