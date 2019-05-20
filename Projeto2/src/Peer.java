import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.InetAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

public class Peer implements RemoteInterface {
    private int id;
    private int port;
    private String address;
    private int manager_port;
    private String manager_address;
    private ConcurrentHashMap<String, SaveFile> files;
    private ScheduledThreadPoolExecutor thread_executor;
    private SSLContext context;

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

        if(this.set_context()) {
            return;
        }

        //Join the network
        try {
            Message message = new Message("JOIN", this.id, null, -1, null, this.address, this.port, null);
            SSLSocketFactory socketfactory = this.context.getSocketFactory();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(this.manager_address, this.manager_port);
            socket.getOutputStream().write(message.build());
            byte[] data = new byte[16000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            byte[] message_data = buffer.toByteArray();
            String answer = new String(message_data);
            if(answer.equals("ERROR ID")) {
                System.out.println("Peer with id " + this.id + " already exists.");
                return;
            } else if(answer.equals("ACK")) {
                System.out.println("Successfully joined the system.");
            }
        } catch(Exception ex) {
            System.out.println("Error connecting to server.");
            ex.printStackTrace();
            return;
        }
        
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
        this.thread_executor.execute(new SendActiveMessage(this));
    }

    public boolean set_context() {
        try {
            this.context = SSLContext.getInstance("TLS");
            KeyManagerFactory key_manager_factory = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory trust_manager_factory = TrustManagerFactory.getInstance("SunX509");
            char[] passphrase = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore ks2 = KeyStore.getInstance("JKS");
    
            ks.load(new FileInputStream("keystore.jks"), passphrase);
            ks2.load(new FileInputStream("truststore.ts"), passphrase);
            trust_manager_factory.init(ks2);
            key_manager_factory.init(ks, passphrase);
            this.context.init(key_manager_factory.getKeyManagers(), trust_manager_factory.getTrustManagers(), null);
        } catch(Exception ex) {
            System.out.println("Error setting up context");
            return true;
        }
        return false;
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

    public SSLContext get_context() {
        return this.context;
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