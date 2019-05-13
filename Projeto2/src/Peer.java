import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.net.*;
import java.io.*;
import java.util.*;

public class Peer implements RemoteInterface {
    private int id;
    private int port;
    private String address;
    private int manager_port;
    private String manager_address;
    private ConcurrentHashMap<String, SaveFile> files;
    private ScheduledThreadPoolExecutor thread_executor;

    public Peer(int id, String remote_object_name, int port, String manager_address, int manager_port) {
        this.id = id;
        this.port = port;
        this.manager_port = manager_port;
        this.manager_address = manager_address;
        this.files = new ConcurrentHashMap<String, SaveFile>();
        this.thread_executor = new ScheduledThreadPoolExecutor(300);

        try{
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception ex) {
            System.out.println("Error getting address.");
            return;
        }

        try {
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remote_object_name, stub);
            System.out.println("Peer ready.");

        } catch (Exception e) {
            System.err.println("Peer exception: Object already bound.");
        }

        //TODO - send JOIN message to PeerManager
        try {
            Socket socket = new Socket(manager_address, manager_port);
            String message = "hello";
            socket.getOutputStream().write(message.getBytes());
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error connecting to server.");
        }
    }

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }

    public int get_id() {
        return this.id;
    }

    public ScheduledThreadPoolExecutor get_thread_executor() {
        return this.thread_executor;
    }

    public ConcurrentHashMap<String, SaveFile> get_files() {
        return this.files;
    }

    public synchronized String backup_file(String file_name, int rep_degree) throws RemoteException {
        System.out.println("Initiated backup of a file.");
        SaveFile file = new SaveFile(file_name, rep_degree);
        if (this.files.get(file_name) != null) {
            return "File already exists";
        }
        this.files.put(file_name, file);
        
        //TODO - send BACKUP message to PeerManager (new thread for that)

        System.out.println("Returned from backup of a file.");
        return "Backup executed successfully.";
    }

    public String restore_file(String file_name) throws RemoteException {
        System.out.println("Initiated restore of a file.");

        SaveFile file = this.files.get(file_name);      
        if(file == null)
            return "File not found.";
        String file_id = file.get_id();
        
        //TODO - send RESTORE message to PeerManager (new thread for that)

        System.out.println("Returned from restore of a file.");
        return "File restored successfully.";
    }
    
    public String delete_file(String file_name) throws RemoteException {
        System.out.println("Initiated delete of a file.");

        SaveFile file = this.files.get(file_name);       
        if(file == null)
            return "File not found.";
        String file_id = file.get_id();

        //TODO - send DELETE message to PeerManager (new thread for that)

        System.out.println("Returned from delete of a file.");
        return "File deleted successfully.";
    }

    public static void main(String[] args) {
        if(args.length != 5) {
            System.out.println("Usage: java Peer <id> <remote_object_name> <port> <manager_ip> <manager_port>");
            return;
        }

        Peer p = new Peer(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
    }
}