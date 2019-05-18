import java.io.File;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.net.URI;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;


public class SaveFile {
    private String id;
    private int rep_degree;
    private File file;
    private String file_name;
    private ArrayList<byte[]> body;
    private Peer peer;

    SaveFile(String file_path, int rep_degree) {
        this.file = new File(file_path);
        this.rep_degree = rep_degree;
        String unhashed_id = file.getName(); //ask number id to identify file, to allow multiple files with the same name in the system TODO
        this.id = this.generate_id(unhashed_id);
        body = new ArrayList<byte[]>();
        
        try {
            byte[] buffer = new byte[16000];
            FileInputStream file_is = new FileInputStream(this.file);
            BufferedInputStream buffered_is = new BufferedInputStream(file_is);
            int num_buf = buffered_is.read(buffer);
            while(num_buf > 0) {
                byte[] buf = Arrays.copyOfRange(buffer, 0, num_buf);
                this.body.add(buf);
                buffer = new byte[16000];
                num_buf = buffered_is.read(buffer);
            }
        } catch(Exception ex) {
            System.out.println("Error reading from file.");
        }
    }

    SaveFile(String file_name, String id, int rep_degree) {
        this.file_name = file_name;
        this.id = id;
        this.rep_degree = rep_degree;
    }

    SaveFile(String file_name, Peer peer) {
        this.file_name = file_name;
        this.peer = peer;
        this.id = this.peer.get_files().get(this.file_name).get_id();
        try {
            File file = new File("peer" + this.peer.get_id() + "/restored/" + this.file_name);
            file.createNewFile();            
        } catch(Exception ex) {
            System.out.println("Error creating restored file.");
        }
    }

    SaveFile(int peer_id, String file_name, ArrayList<byte[]> file) {
        File peer_dir = new File("peer" + peer_id);
        if(!peer_dir.exists()) {
            peer_dir.mkdir();
        }
        File backup_dir = new File("peer" + peer_id + "/backup");
        if(!backup_dir.exists()) {
            backup_dir.mkdir();
        }
        try {
            FileOutputStream fos = new FileOutputStream("peer" + peer_id + "/backup/" + file_name);
            for(int i = 0; i < file.size(); i++) {
                fos.write(file.get(i));
            }
            fos.close();
        } catch(Exception ex) {
            System.out.println("Error in writing to restored file.");
            ex.printStackTrace();
        }
    }

    /******************** Getters *********************/

    public String get_id() {
        return this.id;
    }

    public File get_file(){
        return this.file;
    }

    public String get_name(){
        return this.file_name;
    }

    public int get_rep_degree(){
        return this.rep_degree;
    }

    public ArrayList<byte[]> get_body(){
        return this.body;
    }

    /******************** Others *********************/
    public static String generate_id(String unhashed_id) {
        String to_return = "";
         try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded_hash = digest.digest(unhashed_id.getBytes(StandardCharsets.UTF_8));
            StringBuffer hex_string = new StringBuffer();
            for (int i = 0; i < encoded_hash.length; i++) {
                String hex = Integer.toHexString(0xff & encoded_hash[i]);
                if(hex.length() == 1) hex_string.append('0');
                    hex_string.append(hex);
            }
            to_return = hex_string.toString();
        } catch(Exception e) {
            System.out.println("Error in SHA-256.");
        }

        return to_return;
    }
}
