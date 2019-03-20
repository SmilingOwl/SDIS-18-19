/*
To run: java Peer peer_id remote_obj_name mc_addr mc_port mdb_addr mdb_port
java Peer 1 obj 224.0.0.3 1111 224.0.0.3 2222
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements RemoteInterface{
    private MCThread mc_channel;
    private MDBThread mdb_channel;
    private InetAddress mc_address;
    private int mc_port;
    private InetAddress mdb_address;
    private int mdb_port;
    private int id;
    private ArrayList<SaveFile> myFiles;
    private ArrayList<Chunk> myChunks;

    Peer(int id, InetAddress mc_address, int mc_port, InetAddress mdb_address, int mdb_port, String remote_object_name) {
        this.id = id;
        this.mc_port = mc_port;
        this.mc_address = mc_address;
        this.mc_channel = new MCThread(this.mc_address, this.mc_port, this);
        this.mdb_port = mdb_port;
        this.mdb_address = mdb_address;
        this.mdb_channel = new MDBThread(this.mdb_address, this.mdb_port, this);
        this.myFiles = new ArrayList<>();
        this.myChunks = new ArrayList<>();
        
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

        mdb.start();
        mc.start();
    }

    public static void main(String[] args) {
        if(args.length != 6) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }

        InetAddress mc_address = null, mdb_address = null;

        try {
            mc_address = InetAddress.getByName(args[2]);
            mdb_address = InetAddress.getByName(args[4]);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        if(mc_address==null)
            System.exit(-1);
        int mc_port = Integer.parseInt(args[3]);
        int mdb_port = Integer.parseInt(args[5]);
        String remote_object_name = args[1];
        int peer_id = Integer.parseInt(args[0]);

        Peer peer = new Peer(peer_id, mc_address, mc_port, mdb_address, mdb_port, remote_object_name);
        
    }

    public void receiveMessageMC(byte[] message) {
        Message m = new Message(message);
        if(m.get_type().equals("STORED")) {
            for(int i = 0; i < this.myFiles.size(); i++) {
                if(this.myFiles.get(i).get_id().equals(m.get_file_id())) {
                    this.myFiles.get(i).get_chunks().get(m.get_chunk_no()-1).increase_curr_rep_degree(m.get_sender_id());
                    break;
                }
            }
            for(int j = 0; j < this.myChunks.size(); j++) {
                if(this.myChunks.get(j).get_file_id().equals(m.get_file_id()) && this.myChunks.get(j).get_chunk_no() == m.get_chunk_no()) {
                    this.myChunks.get(j).increase_curr_rep_degree(m.get_sender_id());
                    break;
                }
            }
        }
    }

    public void receiveMessageMDB(byte[] message) {
        Message m = new Message(message);
        if(m.get_type().equals("PUTCHUNK")) {
            boolean found_file =  false;
            if(m.get_sender_id() != this.id) {
                boolean found = false;
                for(int i = 0; i < this.myChunks.size(); i++) {
                    if(m.get_file_id().equals(this.myChunks.get(i).get_file_id()) && m.get_chunk_no() == this.myChunks.get(i).get_chunk_no())
                        found = true;
                }
                if(!found) {
                    Chunk new_chunk = new Chunk(m.get_file_id(), m.get_rep_degree(), m.get_body(), m.get_chunk_no());
                    this.myChunks.add(new_chunk);
                }
                String aux = " ";
                Message send_m = new Message("STORED", "1.0", this.id, m.get_file_id(), m.get_chunk_no(), 0, aux.getBytes());
                Random rand = new Random();
                int random_delay = rand.nextInt(401);
                this.sendMessageMC(send_m.build(), random_delay);
            }
        }
    }

    public void sendMessageMC(byte[] message, int random_delay) {
        mc_channel.sendMessage(message, random_delay);
    }

    public String backup_file(String file_name, int rep_degree) throws RemoteException {
        SaveFile file = new SaveFile(file_name, rep_degree);
        this.myFiles.add(file);
        ArrayList<Chunk> chunks_to_send = file.get_chunks();
        for(int i  = 0; i < chunks_to_send.size(); i++) {
            this.backup_chunk(chunks_to_send.get(i));
        }
        return "backup done";
    }

    private void backup_chunk(Chunk chunk) {
        Message message = new Message("PUTCHUNK", "1.0", this.id, chunk.get_file_id(), chunk.get_chunk_no(), chunk.get_rep_degree(), chunk.get_body());
        MulticasterPutChunkThread send_chunk_thread = new MulticasterPutChunkThread(this.mdb_address, this.mdb_port, message.build(), chunk);
        send_chunk_thread.run();
    }
    
    public String restore_file(String file_name) throws RemoteException {
        System.out.println("---------FOR TESTING BACK UP PURPOSES ONLY -> NOT YET DONE---------");
        try {
            File file = new File("a.pdf");
            file.createNewFile();            
            FileOutputStream fos = new FileOutputStream("a.pdf");
            for(int i = 0; i < myChunks.size(); i++) {
                fos.write(myChunks.get(i).get_body());
            }
            fos.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return "initiated restore";
    }
    public String delete_file(String file_name) throws RemoteException {
        return "initiated delete";
    }
    public String reclaim(int max_ammount) throws RemoteException {
        return "initiated reclaim";
    }
    public String state() throws RemoteException {
        return "initiated state";
    }
}