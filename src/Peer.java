/*
To run: java Peer peer_id remote_obj_name mc_addr mc_port mdb_addr mdb_port
java Peer 1 obj 224.0.0.3 1111 224.0.0.3 2222 224.0.0.3 3333
 */
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.net.InetAddress;
import java.io.File;

public class Peer implements RemoteInterface{
    private MCThread mc_channel;
    private MDBThread mdb_channel;
    private MRThread mdr_channel;
    private InetAddress mc_address;
    private int mc_port;
    private InetAddress mdb_address;
    private int mdb_port;
    private InetAddress mdr_address;
    private int mdr_port;
    private int id;
    private ScheduledThreadPoolExecutor thread_executor;
    private ConcurrentHashMap<String, String> myFiles;
    private ConcurrentHashMap<String, SaveFile> myFilesToRestore;
    private ArrayList<String> myChunksNotToSend;
    private ConcurrentHashMap<String, Integer> files_size;
    private ConcurrentHashMap<String, ArrayList<Integer>> chunk_occurrences;
    private ArrayList<Chunk> myChunks;
    private int maxFreeSpace = 1000000000;
    private int free_space;

    Peer(int id, InetAddress mc_address, int mc_port, InetAddress mdb_address, int mdb_port, InetAddress mdr_address, 
                                int mdr_port, String remote_object_name) {
        this.id = id;
        this.mc_port = mc_port;
        this.mc_address = mc_address;
        this.mc_channel = new MCThread(this.mc_address, this.mc_port, this);
        this.mdb_port = mdb_port;
        this.mdb_address = mdb_address;
        this.mdb_channel = new MDBThread(this.mdb_address, this.mdb_port, this);
        this.mdr_port = mdr_port;
        this.mdr_address = mdr_address;
        this.mdr_channel = new MRThread(this.mdr_address, this.mdr_port, this);
        this.myChunks = new ArrayList<>();
        this.myFiles = new ConcurrentHashMap<String, String>();
        this.myFilesToRestore = new ConcurrentHashMap<String, SaveFile>();
        this.myChunksNotToSend = new ArrayList<String>();
        this.files_size = new ConcurrentHashMap<String, Integer>();
        this.chunk_occurrences = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.thread_executor = new ScheduledThreadPoolExecutor(300);
        this.free_space = this.maxFreeSpace;

        File peer_dir = new File("peer" + this.id);
        if(!peer_dir.exists()) {
            peer_dir.mkdir();
        }
        File backup_dir = new File("peer" + this.id + "/backup");
        if(!backup_dir.exists()) {
            backup_dir.mkdir();
        }
        File restore_dir = new File("peer" + this.id + "/restored");
        if(!restore_dir.exists()) {
            restore_dir.mkdir();
        }
        
        try {
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remote_object_name, stub);
            System.out.println("Peer ready");

        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }

        Thread mdb = new Thread(this.mdb_channel);
        Thread mc = new Thread(this.mc_channel);
        Thread mdr = new Thread(this.mdr_channel);

        mdb.start();
        mc.start();
        mdr.start();
    }

    public InetAddress get_mdb_address() {
        return this.mdb_address;
    }

    public int get_mdb_port() {
        return this.mdb_port;
    }

    public int get_occupied_space(){
        int occupied_space = 0;
        for(int i = 0; i < this.myChunks.size(); i++) {
            occupied_space += this.myChunks.get(i).get_body().length;
        }
        return occupied_space;
    }

    public int get_free_space() {
        return this.free_space;
    }

    public void add_to_free_space(int occupied) {
        this.free_space += occupied;
    }

    public InetAddress get_mdr_address() {
        return this.mdr_address;
    }

    public int get_mdr_port() {
        return this.mdr_port;
    }

    public ArrayList<Chunk> get_chunks() {
        return this.myChunks;
    }

    public ScheduledThreadPoolExecutor get_thread_executor() {
        return this.thread_executor;
    }

    public int get_id() {
        return this.id;
    }

    public void add_chunk_not_to_send(String file_id, int chunk_no) {
        this.myChunksNotToSend.add(file_id + ":" + chunk_no);
    }

    public ArrayList<String> get_myChunksNotToSend() {
        return this.myChunksNotToSend;
    }

    public static void main(String[] args) {
        if(args.length != 8) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }

        InetAddress mc_address = null, mdb_address = null, mdr_address = null;

        try {
            mc_address = InetAddress.getByName(args[2]);
            mdb_address = InetAddress.getByName(args[4]);
            mdr_address = InetAddress.getByName(args[6]);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(mc_address==null)
            System.exit(-1);

        int mc_port = Integer.parseInt(args[3]);
        int mdb_port = Integer.parseInt(args[5]);
        int mdr_port = Integer.parseInt(args[7]);
        String remote_object_name = args[1];
        int peer_id = Integer.parseInt(args[0]);
        Peer peer = new Peer(peer_id, mc_address, mc_port, mdb_address, mdb_port, mdr_address, mdr_port, remote_object_name);
    }

    public ConcurrentHashMap<String, ArrayList<Integer>> get_chunk_occurrences() {
        return this.chunk_occurrences;
    }

    public ConcurrentHashMap<String, String> get_files() {
        return this.myFiles;
    }

    public ConcurrentHashMap<String, SaveFile> get_myFilesToRestore() {
        return this.myFilesToRestore;
    }

    public void sendMessageMC(byte[] message) {
        mc_channel.sendMessage(message);
    }

    public String backup_file(String file_name, int rep_degree) throws RemoteException {
        
        SaveFile file = new SaveFile(file_name, rep_degree);
        if (this.files_size.get(file.get_id()) != null) {
            return "File already exists";
        }
        this.myFiles.put(file_name, file.get_id());
        this.files_size.put(file.get_id(), file.get_chunks().size());
        ArrayList<Chunk> chunks_to_send = file.get_chunks();
      
        for(int i  = 0; i < chunks_to_send.size(); i++) {
            this.backup_chunk(chunks_to_send.get(i));
        }
        return "Backup executed successfully.";
    }

    public void backup_chunk(Chunk chunk) {
        Message message = new Message("PUTCHUNK", "1.0", this.id, chunk.get_file_id(), chunk.get_chunk_no(), chunk.get_rep_degree(), chunk.get_body());
           
        this.get_thread_executor().execute(
                        new MulticasterPutChunkThread(this.mdb_address, this.mdb_port, message.build(), chunk, this));
    }
    
    public String restore_file(String file_name) throws RemoteException {
        String file_id = this.myFiles.get(file_name);      
        if(file_id == null)
            return "File not found.";
        
        int number_of_chunks = this.files_size.get(file_id);
        SaveFile new_file = new SaveFile(file_name, number_of_chunks, this);
        this.myFilesToRestore.put(file_id, new_file);
       
        for(int i = 0; i < number_of_chunks; i++) {
            Message to_send = new Message("GETCHUNK", "1.0", this.id, file_id, i+1, 0, null);
            this.sendMessageMC(to_send.build());
        }
        return "File restored successfully.";
    }
    
    public String delete_file(String file_name) throws RemoteException {
        String file_id = this.myFiles.get(file_name);       
        if(file_id == null)
            return "File not found.";
        
        Message to_send = new Message("DELETE", "1.0", this.id, file_id, 0, 0, null);
        this.sendMessageMC(to_send.build());

        this.myFiles.remove(file_name);
        this.files_size.remove(file_id);
        this.chunk_occurrences.remove(file_id);
        return "File deleted successfully.";
    }

    public String reclaim(int max_ammount) throws RemoteException {
        this.maxFreeSpace = max_ammount;
        this.free_space = this.maxFreeSpace - this.get_occupied_space();
        int i = 0;
        while(this.free_space < 0) {
            if(i < this.myChunks.size()) {
                String chunk_name = this.myChunks.get(i).get_file_id() + ":" + this.myChunks.get(i).get_chunk_no();
                ArrayList<Integer> occurrences_list = chunk_occurrences.get(chunk_name);
                if(occurrences_list != null) {
                    int occurrences = occurrences_list.size();
                    if(occurrences < this.myChunks.get(i).get_rep_degree()) {
                        int chunk_n = this.myChunks.get(i).get_chunk_no();
                        String file_id = this.myChunks.get(i).get_file_id();
                        int occupied = this.myChunks.get(i).get_body().length;
                        this.add_to_free_space(occupied);
                        System.out.println("After removing chunk on reclaim, I have " + this.get_free_space() + " available");
                        File currentFile = new File("peer" + this.id + "/backup/" 
                                        + this.myChunks.get(i).get_file_id() + "/chk" + this.myChunks.get(i).get_chunk_no());
                        if(!currentFile.exists()) {
                            System.out.println("Trying to delete chunk on reclaim. File doesn't exist.");
                            continue;
                        }
                        currentFile.delete();
                        Message message = new Message("REMOVED", "1.0", this.id, file_id, chunk_n, 0, null);
                        this.sendMessageMC(message.build());
                        //CHECK IF FOLDER IS EMPTY!
                        this.myChunks.remove(i);
                        i--;
                    }
                }
            } else 
                break;
            i++;
        }
        i = 0;
        while(this.free_space < 0) {
            if(i < this.myChunks.size()) {
                int chunk_n = this.myChunks.get(i).get_chunk_no();
                String file_id = this.myChunks.get(i).get_file_id();
                String chunk_name = this.myChunks.get(i).get_file_id() + ":" + this.myChunks.get(i).get_chunk_no();
                int occupied = this.myChunks.get(i).get_body().length;
                this.add_to_free_space(occupied);
                System.out.println("After removing chunk on reclaim, I have " + this.get_free_space() + " available");
                File currentFile = new File("peer" + this.id + "/backup/" 
                                + this.myChunks.get(i).get_file_id() + "/chk" + this.myChunks.get(i).get_chunk_no());
                if(!currentFile.exists()) {
                    System.out.println("Trying to delete chunk on reclaim. File doesn't exist.");
                    continue;
                }
                currentFile.delete();
                Message message = new Message("REMOVED", "1.0", this.id, file_id, chunk_n, 0, null);
                this.sendMessageMC(message.build());
                this.myChunks.remove(i);
                i--;
            }
            else break;
            i++;
        }
        return "initiated reclaim";
    }

    public String state() throws RemoteException {
       
        // for each file whose backup it has initiated:
        /*for(int i=0; i< ; i++){

            System.out.println("FILE PATHNAME: " + "\n");
            System.out.println("FILE ID: " +"\n");
            System.out.println("FILE REPLICATION DEGREE: " + "\n");

            //For each chunk of the file:
            for( int j=0; j< ; j++){
                System.out.println("CHUNK ID: " + "\n");
                System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + "\n");

            }
        }*/

        // for each chunk it stores:
        for (int i=0; i< myChunks.size(); i++){
            System.out.println("CHUNK ID: " + myChunks.get(i).get_chunk_no() +"\n");
            System.out.println("CHUNK SIZE: " + myChunks.get(i).get_body().length + "\n");
            System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + myChunks.get(i).get_curr_rep_degree()+ "\n");
        }

        // the maximum amount of disk space that can be used to store chunks
        int free_space= this.maxFreeSpace - get_occupied_space();
        System.out.println("MAXIMUM AMOUNT OF THE DISK SPACE TO STORE CHUNKS:" + free_space + "\n");

        // the amount of storage used to backup the chunks
        System.out.println("STORAGE USED TO BACKUP THE CHUNKS: " + get_occupied_space() + "\n");
        return "initiated state";
    }
}