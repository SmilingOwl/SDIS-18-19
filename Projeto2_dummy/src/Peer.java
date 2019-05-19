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
        this.thread_executor = new ScheduledThreadPoolExecutor(3000);

        try{
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception ex) {
            System.out.println("Error getting address.");
            return;
        }

        //Join the network
        Message message = new Message("JOIN", this.id, null, -1, null, this.address, this.port, null);
        SendMessage send_join = new SendMessage(this.manager_address, this.manager_port, message);
        send_join.run();
        
        //Set up RMI
        try {
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remote_object_name, stub);
            System.out.println("Peer ready.");

        } catch (Exception e) {
            System.out.println("Error on RMI: Check if you've started rmiregistry by doing 'start rmiregistry' on Windows"
                + " or 'rmiregistry &' on Ubuntu. If so, check if object is already bound.");
            return;
        }

        this.thread_executor.execute(new ConnectionThread(this.port, this.thread_executor, this));
    }

    /************************** Getters **************************/

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }

    public int get_manager_port() {
        return this.manager_port;
    }

    public String get_manager_address() {
        return this.manager_address;
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

    /************************** Protocols functions **************************/

    public synchronized String backup_file(String file_name, int rep_degree) throws RemoteException {
        this.thread_executor.execute(new BackupThread(file_name, rep_degree, this));
        return "Backup executed successfully.";
    }

    public String restore_file(String file_name) throws RemoteException {
        this.thread_executor.execute(new RestoreThread(file_name, this));
        return "File restored successfully.";
    }
    
    public String delete_file(String file_name) throws RemoteException {
        this.thread_executor.execute(new DeleteThread(file_name, this));
        return "File deleted successfully.";
    }
    
    /************************** Main function **************************/
    public static void main(String[] args) {
        if(args.length != 5) {
            System.out.println("Usage: java -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password Peer <id> <remote_object_name> <port> <manager_ip> <manager_port>");
            return;
        }

        new Peer(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
    }
}