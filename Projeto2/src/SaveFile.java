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


public class SaveFile {
    private String id;
    private int rep_degree;
    private File file;
    private String file_name;
    private byte[] body;
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
            System.out.println("Error in SHA-256.");
        }

        try {
            Path path = Paths.get(URI.create( this.getClass().getResource(file_path).toString()));
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            
            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    body = new byte[attachment.remaining()];
                    attachment.get(body, 0, body.length);
                }
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("Reading from file failed.");
                }
            });
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
}
