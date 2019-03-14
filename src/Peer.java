/*
To run: java Peer peer_id remote_obj_name mc_addr mc_port mdb_addr mdb_port
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
    MCThread mc_channel;
    MDBThread mdb_channel;
    InetAddress mc_address;
    int mc_port;
    int id;
    ArrayList<SaveFile> myFiles;
    ArrayList<Chunk> myChunks;

    Peer(int id, InetAddress mc_address, int mc_port, InetAddress mdb_address, int mdb_port, String remote_object_name) {
        this.id = id;
        this.mc_port = mc_port;
        this.mc_address = mc_address;
        this.mc_channel = new MCThread(this.mc_address, this.mc_port, this);
        this.mdb_channel = new MDBThread(mdb_address, mdb_port, this);
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

        this.mc_channel.run();
        this.mdb_channel.run();
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

    public void receiveMessageMC(String message) {
        System.out.println("In peer: " + message);
        //if STORE -> if peer has chunk, increase count
    }

    public void receiveMessageMDB(String message) {
        System.out.println("In peer: " + message);
        //interpret message
        //if putchunk -> send message store through MC
    }

    public void sendMessageMC(String message) {
        mc_channel.sendMessage(message);
    }

    public String backup_file(String file_name, int rep_degree) throws RemoteException {
        MulticasterPutChunkThread send_chunk_thread = new MulticasterPutChunkThread(this.mc_address, this.mc_port, file_name);
        send_chunk_thread.run();
        SaveFile file = new SaveFile(file_name, rep_degree);
        this.myFiles.add(file);
        //ciclo for para cada um dos chunks do ficheiro: bachup_chunk()
        return "initiated backup";
    }

    private void backup_chunk() {
        //create putchunk message
        //this.sendMessageMDB(putchunk_message);
        //int messages_received;
        //for 1 sec receive messages from mc
        //if(messages_received < rep_degree) retransmit putchunk message on MDB channel (up to 5 times)
    }
    
    public String restore_file(String file_name) throws RemoteException {
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