import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Peer implements RemoteInterface {
    private BigInteger id;
    private int port;
    private String address;
    private ConcurrentHashMap<String, SaveFile> files;
    private ScheduledThreadPoolExecutor thread_executor;

    public Peer(String remote_object_name, int port) {
        this.port = port;
        this.files = new ConcurrentHashMap<String, SaveFile>();
        this.thread_executor = new ScheduledThreadPoolExecutor(300);

        try{
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception ex) {
            System.out.println("Error getting address.");
            return;
        }

        this.generateId();

        try {
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remote_object_name, stub);
            System.out.println("Peer ready.");

        } catch (Exception e) {
            System.err.println("Peer exception: Object already bound.");
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

    public BigInteger get_id() {
        return this.id;
    }

    public ScheduledThreadPoolExecutor get_thread_executor() {
        return this.thread_executor;
    }

    public ConcurrentHashMap<String, SaveFile> get_files() {
        return this.files;
    }

    /************************** Others **************************/
    private void generateId() {
        byte[] result = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            String input = this.address + " " + this.port;
            result = mDigest.digest(input.getBytes());
        } catch(NoSuchAlgorithmException ex) {
            System.out.println("Error generating id");
        }
        this.id = new BigInteger(result);
    }

    /************************** Protocols functions **************************/

    public synchronized String backup_file(String file_name, int rep_degree) throws RemoteException {
        //this.thread_executor.execute(new BackupThread(file_name, rep_degree, this));
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
    
    /************************** Main function **************************/
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: java -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password Peer <remote_object_name> <port>");
            return;
        }

        Peer p = new Peer(args[0], Integer.parseInt(args[1]));
    }
}