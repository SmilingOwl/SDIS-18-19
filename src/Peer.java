/*
To run: java Peer mcast_addr mcast_port
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
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }
        String remote_object_name = args[0];

        try {
            Peer obj = new Peer();
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remote_object_name, stub);
            System.out.println("Peer ready");
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public String backup_file(String file_name, int rep_degree) throws RemoteException {
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