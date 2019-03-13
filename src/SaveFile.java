import java.io.File;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SaveFile {
    private String id;
    private int rep_degree;

    SaveFile(String file_path, int rep_degree) {
        File file = new File(file_path);
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
        System.out.println("generated id: " + this.id);

        this.split_file();
    }

//TODO: this function + class Chunk
    public void split_file() {

    }
}