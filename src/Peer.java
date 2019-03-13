/*
To run: java Peer remote_obj_name mc_addr mc_port mdb_addr mdb_port
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
    Peer(InetAddress mc_address, int mc_port, InetAddress mdb_address, int mdb_port, String remote_object_name) {

        this.mc_channel = new MCThread(mc_address, mc_port, this);
        this.mdb_channel = new MDBThread(mdb_address, mdb_port, this);
        
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
        if(args.length != 5) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }

        InetAddress mc_address = null, mdb_address = null;

        try {
            mc_address = InetAddress.getByName(args[1]);
            mdb_address = InetAddress.getByName(args[3]);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        if(mc_address==null)
            System.exit(-1);
        int mc_port = Integer.parseInt(args[2]);
        int mdb_port = Integer.parseInt(args[4]);
        String remote_object_name = args[0];

        Peer peer = new Peer(mc_address, mc_port, mdb_address, mdb_port, remote_object_name);
        
    }

    public void receiveMessage(String message) {
        System.out.println("In peer: " + message);
    }

    public void sendMessageMC(String message) {
        mc_channel.sendMessage(message);
    }

    public String backup_file(String file_name, int rep_degree) throws RemoteException {
        this.sendMessageMC("Hello!!");
        return "initiated backup";
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