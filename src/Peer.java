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
            Server obj = new Server();
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remote_object_name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    String backup_file(String file_name, int rep_id) throws RemoteException {}
    String restore_file(String file_name) throws RemoteException {}
    String delete_file(String file_name) throws RemoteException {}
    String reclaim(int max_ammount) throws RemoteException {}
    String state() throws RemoteException {}
}