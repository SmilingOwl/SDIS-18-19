/*
To run: java Peer peer_id remote_obj_name mc_addr mc_port mdb_addr mdb_port
java Peer 1 obj 224.0.0.3 1111 224.0.0.3 2222
 */
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.net.InetAddress;

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

    public int get_occupied_space(){
        int occupied_space=0;
        for(int i=0; i< myChunks.size(); i++){
            occupied_space += myChunks.get(i).get_body().length;
        }
        return occupied_space;
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

    public ConcurrentHashMap<String, SaveFile> get_myFilesToRestore() {
        return this.myFilesToRestore;
    }

    public void sendMessageMC(byte[] message) {
        mc_channel.sendMessage(message);
    }

    public String backup_file(String file_name, int rep_degree) throws RemoteException {
        
        SaveFile file = new SaveFile(file_name, rep_degree);
        this.myFiles.put(file_name, file.get_id());
        this.files_size.put(file.get_id(), file.get_chunks().size());
        ArrayList<Chunk> chunks_to_send = file.get_chunks();
      
        for(int i  = 0; i < chunks_to_send.size(); i++) {
            this.backup_chunk(chunks_to_send.get(i));
        }
        return "Backup executed successfully";
    }

    private void backup_chunk(Chunk chunk) {
        Message message = new Message("PUTCHUNK", "1.0", this.id, chunk.get_file_id(), chunk.get_chunk_no(), chunk.get_rep_degree(), chunk.get_body());
      
        MulticasterPutChunkThread send_chunk_thread = 
            new MulticasterPutChunkThread(this.mdb_address, this.mdb_port, message.build(), chunk, this);
      
            send_chunk_thread.run();
    }
    
    public String restore_file(String file_name) throws RemoteException {
        String file_id = this.myFiles.get(file_name);
        int number_of_chunks = this.files_size.get(file_id);
      
        if(file_id == null)
            return "File not found";
        
        SaveFile new_file = new SaveFile(file_name, number_of_chunks, this);
        this.myFilesToRestore.put(file_id, new_file);
       
        for(int i = 0; i < number_of_chunks; i++) {
            Message to_send = new Message("GETCHUNK", "1.0", this.id, file_id, i+1, 0, null);
            this.sendMessageMC(to_send.build());
        }
        return "initiated restore";
    }
    
    public String delete_file(String file_name) throws RemoteException {
        String file_id = this.myFiles.get(file_name);
        int number_of_chunks = this.files_size.get(file_id);
       
        if(file_id == null)
            return "File not found";
        
        for(int i=0; i< number_of_chunks; i++){
            Message to_send = new Message("DELETE", "1.0", this.id, file_id, 0, 0, null);
            this.sendMessageMC(to_send.build());
        }
        return "initiated delete";
    }

    public String reclaim(int max_ammount) throws RemoteException {
        int free_space= get_occupied_space()-max_ammount;

        if(free_space > 0){

        }else{
            
        }
        return "initiated reclaim";
    }

    public String state() throws RemoteException {
        int number_of_chunks = this.files_size.size();
        for(int i=0; i< number_of_chunks; i++){

            System.out.println("FILE PATHNAME: " + "\n");
            System.out.println("FILE ID: " +"\n");
            System.out.println("FILE REPLICATION DEGREE: " + "\n");
        }
        return "initiated state";
    }
}