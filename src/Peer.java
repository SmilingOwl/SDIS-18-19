/*
To run: java Peer peer_id remote_obj_name version mc_addr mc_port mdb_addr mdb_port
java Peer 1 obj 1.0 224.0.0.3 1111 224.0.0.3 2222 224.0.0.3 3333
 */
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.InetAddress;
import java.io.File;
import java.nio.file.Files;

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
    private String version;
    private ScheduledThreadPoolExecutor thread_executor;
    private ConcurrentHashMap<String, SaveFile> myFiles;
    private ConcurrentHashMap<String, SaveFile> myFilesToRestore;
    private ArrayList<String> myChunksNotToSend;
    private ConcurrentHashMap<String, ArrayList<Integer>> chunk_occurrences;
    private ArrayList<Chunk> myChunks;
    private int maxFreeSpace = 100000000;
    private int free_space;

    Peer(int id, String version, InetAddress mc_address, int mc_port, InetAddress mdb_address, int mdb_port, InetAddress mdr_address, 
                                int mdr_port, String remote_object_name) {
        this.id = id;
        this.version = version;
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
        this.myFiles = new ConcurrentHashMap<String, SaveFile>();
        this.myFilesToRestore = new ConcurrentHashMap<String, SaveFile>();
        this.myChunksNotToSend = new ArrayList<String>();
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
        } else {
            File[] files = backup_dir.listFiles(); 
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i].getName());
                for(int j = 0; j < files[i].listFiles().length; j++) {
                    String number = files[i].listFiles()[j].getName().substring(3, files[i].listFiles()[j].getName().length());
                    int chunk_number = Integer.parseInt(number);
                    System.out.println(chunk_number);
                    byte[] body = null;
                    try {
                        body = Files.readAllBytes(new File("peer" + this.id + "/backup/" + files[i].getName() + "/" + files[i].listFiles()[j].getName()).toPath());
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    Chunk chunk_to_add = new Chunk(files[i].getName(), 2, body, chunk_number);
                }
            }
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

    public String get_version() {
        return this.version; 
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
        if(args.length != 9) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }

        InetAddress mc_address = null, mdb_address = null, mdr_address = null;

        try {
            mc_address = InetAddress.getByName(args[3]);
            mdb_address = InetAddress.getByName(args[5]);
            mdr_address = InetAddress.getByName(args[7]);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(mc_address==null)
            System.exit(-1);

        int mc_port = Integer.parseInt(args[4]);
        int mdb_port = Integer.parseInt(args[6]);
        int mdr_port = Integer.parseInt(args[8]);
        String remote_object_name = args[1];
        String version = args[2];
        if(!version.equals("1.0") && !version.equals("2.0")) {
            System.out.println("Error: Unidentified version: " + version);
            return;
        }
        int peer_id = Integer.parseInt(args[0]);
        Peer peer = new Peer(peer_id, version, mc_address, mc_port, mdb_address, mdb_port, mdr_address, mdr_port, remote_object_name);
    }

    public ConcurrentHashMap<String, ArrayList<Integer>> get_chunk_occurrences() {
        return this.chunk_occurrences;
    }

    public ConcurrentHashMap<String, SaveFile> get_files() {
        return this.myFiles;
    }

    public ConcurrentHashMap<String, SaveFile> get_myFilesToRestore() {
        return this.myFilesToRestore;
    }

    public void sendMessageMC(byte[] message) {
        mc_channel.sendMessage(message);
    }

    public synchronized String backup_file(String file_name, int rep_degree) throws RemoteException {
        System.out.println("Initiated backup of a file.");
        
        SaveFile file = new SaveFile(file_name, rep_degree);
        if (this.myFiles.get(file_name) != null) {
            return "File already exists";
        }
        this.myFiles.put(file_name, file);
        ArrayList<Chunk> chunks_to_send = file.get_chunks();
      
        for(int i = 0; i < chunks_to_send.size(); i++) {
            this.backup_chunk(chunks_to_send.get(i));
        }
        System.out.println("Returned from backup of a file.");
        return "Backup executed successfully.";
    }

    public void backup_chunk(Chunk chunk) {
        Message message = new Message("PUTCHUNK", this.version, this.id, chunk.get_file_id(), chunk.get_chunk_no(), chunk.get_rep_degree(), chunk.get_body());
           
        this.get_thread_executor().execute(
                new MulticasterPutChunkThread(this.mdb_address, this.mdb_port, message.build(), chunk, this));
    }
    
    public String restore_file(String file_name) throws RemoteException {
        System.out.println("Initiated restore of a file.");
        SaveFile file = this.myFiles.get(file_name);      
        if(file == null)
            return "File not found.";
        String file_id = file.get_id();
        
        int number_of_chunks = file.get_chunks().size();
        SaveFile new_file = new SaveFile(file_name, number_of_chunks, this);
        this.myFilesToRestore.put(file_id, new_file);
       
        for(int i = 0; i < number_of_chunks; i++) {
            Message to_send = new Message("GETCHUNK", "1.0", this.id, file_id, i+1, 0, null);
            this.sendMessageMC(to_send.build());
        }
        System.out.println("Returned from restore of a file.");
        return "File restored successfully.";
    }
    
    public String delete_file(String file_name) throws RemoteException {
        System.out.println("Initiated delete of a file.");
        SaveFile file = this.myFiles.get(file_name);       
        if(file == null)
            return "File not found.";
        String file_id = file.get_id();
        
        Message to_send = new Message("DELETE", "1.0", this.id, file_id, 0, 0, null);
        this.sendMessageMC(to_send.build());

        int num_chunks = this.myFiles.get(file_name).get_chunks().size();

        this.myFiles.remove(file_name);
        for(int i = 0; i < num_chunks; i++) {
            this.chunk_occurrences.remove(file_id + ":" + (i+1));
        }
        System.out.println("Returned from delete of a file.");
        return "File deleted successfully.";
    }

    public String reclaim(int max_ammount) throws RemoteException {
        System.out.println("Initiated reclaim.");
        this.maxFreeSpace = max_ammount;
        this.free_space = this.maxFreeSpace - this.get_occupied_space();
        this.get_thread_executor().execute(new DoReclaimThread(this));
        System.out.println("Returned from reclaim.");
        return "initiated reclaim";
    }

    public String state() throws RemoteException {
        String info = "";
        if(!myFiles.isEmpty())
            info += "Files backed up: \n\n";
        // for each file whose backup it has initiated:
        for(SaveFile file : myFiles.values())
        { 
            info += "FILE PATHNAME: " + file.get_file().getAbsolutePath() + "\n";
            info += "FILE ID: "+ file.get_id() +"\n";
            info += "FILE REPLICATION DEGREE: " + file.get_rep_degree()+ "\n";

            for(int i = 0; i < file.get_chunks().size(); i++){
                String key = file.get_id() + ":" + file.get_chunks().get(i).get_chunk_no();
                info += "CHUNK ID: "+ file.get_chunks().get(i).get_chunk_no() + "\n";
                info += "CHUNK PERCEIVED REPLICATION DEGREE: " + this.chunk_occurrences.get(key).size() + "\n";
            }
            info += "\n";
        }

        if(myChunks.size() > 0)
            info += "\nStored chunks: \n\n";
        //For each chunk it stores:
        for (int i=0; i< myChunks.size(); i++){
            info += "CHUNK ID: " + myChunks.get(i).get_chunk_no() +"\n";
            info += "CHUNK SIZE: " + myChunks.get(i).get_body().length / 1000.0 + " KBytes\n";
            String key = myChunks.get(i).get_file_id() + ":" + myChunks.get(i).get_chunk_no();
            int rep_degree = 0;
            if(chunk_occurrences.get(key) != null)
                rep_degree = chunk_occurrences.get(key).size();
            info += "CHUNK PERCEIVED REPLICATION DEGREE: " + rep_degree + "\n";
        }

        // the maximum amount of disk space that can be used to store chunks
        info += "MAXIMUM AMOUNT OF THE DISK SPACE TO STORE CHUNKS:" + this.maxFreeSpace /1000.0 + " KBytes\n";

        // the amount of storage used to backup the chunks
        info += "STORAGE USED TO BACKUP THE CHUNKS: " + get_occupied_space() /1000.0 + " KBytes\n";
        return info;
    }
}