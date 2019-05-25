import java.io.File;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.concurrent.Future;


public class SaveFile {
    private String id;
    private int rep_degree;
    private String file_name;
    private ArrayList<byte[]> body;
    private boolean error = false;

    SaveFile(String file_path, int rep_degree) {
        this.rep_degree = rep_degree;
        String unhashed_id = file_path;
        this.id = SaveFile.generate_id(unhashed_id);
        body = new ArrayList<byte[]>();
        
        this.read(file_path);
    }

    SaveFile(String file_name, String id, int rep_degree) {
        this.file_name = file_name;
        this.id = id;
        this.rep_degree = rep_degree;
    }

    SaveFile(int peer_id, String file_name, String protocol, ArrayList<byte[]> file) {
        String[] parts = file_name.split("/");
        String file_to_write = parts[parts.length-1];
        this.body = file;

        File peer_dir = new File("peer" + peer_id);
        if(!peer_dir.exists()) {
            peer_dir.mkdir();
        }
        File backup_dir = new File("peer" + peer_id + "/" + protocol);
        if(!backup_dir.exists()) {
            backup_dir.mkdir();
        }

        try {
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            for(int i = 0; i < file.size(); i++) {
                output_stream.write(file.get(i));
            }
            byte[] file_buffer = output_stream.toByteArray();
            this.write("peer" + peer_id + "/" + protocol + "/" + file_to_write, file_buffer);
        } catch(Exception ex) {
            System.out.println("Error writing to file.");
        }
    }

    /******************** Getters *********************/

    public String get_id() {
        return this.id;
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

    public boolean get_error() {
        return this.error;
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

    /** Functions to write and read from files */

    private void write(String file_path, byte[] content) {
    
        try {
            Path path = Paths.get(file_path);
            if(!Files.exists(path)){
                Files.createFile(path);
            }
            AsynchronousFileChannel file_channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        
            ByteBuffer buffer = ByteBuffer.wrap(content);
            buffer.put(content);
            buffer.flip();

            file_channel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {

                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    System.out.println("Successfully wrote to file.");
                    try {
                        file_channel.close();
                    } catch (Exception ex) {
                        System.out.println("Error closing file.");
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("Error writing to file.");
                }
            }); 
        } catch (Exception ex) {
            System.out.println("Error writing to file.");
        }
    }

    private void read(String file_path) {
        try {
            Path path = Paths.get(file_path);
            File file = path.toFile();
            if(!file.exists()){
                System.out.println("Error: File doesn't exist.");
            }
            
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate((int)file.length());
            Future<Integer> result = channel.read(buffer, 0);
                
            while (!result.isDone()) ;
            
            System.out.println("Reading done from file successfully.");

            buffer.flip();
                
            byte[] content = new byte[buffer.capacity()];
            int i = 0;
            while (buffer.hasRemaining()) {
                content[i] = buffer.get();    
                i++; 
            }
            buffer.clear();

            ByteArrayInputStream input_stream = new ByteArrayInputStream(content);
            byte[] reader_buffer = new byte[16000];
            int num_buf = input_stream.read(reader_buffer);
            while(num_buf > 0) {
                byte[] buf = Arrays.copyOfRange(reader_buffer, 0, num_buf);
                this.body.add(buf);
                reader_buffer = new byte[16000];
                num_buf = input_stream.read(reader_buffer);
            }
            channel.close();
        } catch(Exception ex) {
            System.out.println("Error reading from file.");
            error = true;
        }
    }
}
