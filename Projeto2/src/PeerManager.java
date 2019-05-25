import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.ArrayList;
import java.net.InetAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PeerManager {
    private ConcurrentHashMap<Integer, PeerInfo> peers;
    private ConcurrentHashMap<String, ArrayList<Integer>> files;
    private ArrayList<PeerManagerInfo> managers;
    private int port;
    private String address;
    private int other_manager_port;
    private String other_manager_address;
    private ScheduledThreadPoolExecutor thread_executor;
    private SSLContext context;

    PeerManager(int port, String other_manager_address, int other_manager_port) {
        this.port = port;
        this.other_manager_address = other_manager_address;
        this.other_manager_port = other_manager_port;

        try{
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception ex) {
            System.out.println("Error getting address.");
            return;
        }
        this.peers = new ConcurrentHashMap<Integer, PeerInfo>();
        this.files = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.managers = new ArrayList<PeerManagerInfo>();
        this.thread_executor = new ScheduledThreadPoolExecutor(3000);
        
        if(this.other_manager_address != null) {
            PeerManagerInfo manager = new PeerManagerInfo(this.other_manager_port, this.other_manager_address);
            this.managers.add(manager);
        }

        if(this.set_context())
            return;
        
        if(!this.join_network())
            return;

        this.thread_executor.execute(new ConnectionThread(this.port, this.thread_executor, this));
        this.thread_executor.execute(new CheckActiveThread(this));

        Message warn_managers = new Message("MANAGER_ADD", -1, null, -1, null, this.address, this.port, null);
        for(int i = 1; i < this.managers.size(); i++) {
            if (this.managers.get(i).get_address() == this.address && this.managers.get(i).get_port() == this.port) {
                this.managers.remove(i);
                i--;
                continue;
            }
            SendMessage send_msg = new SendMessage(this.managers.get(i).get_address(), this.managers.get(i).get_port(), 
                warn_managers, this.context.getSocketFactory());
                send_msg.run();
        }
        for(Integer peer_id : this.peers.keySet()) {
            SendMessage send_msg = new SendMessage(this.peers.get(peer_id).get_address(), this.peers.get(peer_id).get_port(), 
                warn_managers, this.context.getSocketFactory());
                send_msg.run();
        }
    }

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }

    public int get_other_manager_port() {
        return this.other_manager_port;
    }

    public String get_other_manager_address() {
        return this.other_manager_address;
    }

    public ConcurrentHashMap<String, ArrayList<Integer>> get_files() {
        return this.files;
    }

    public ConcurrentHashMap<Integer, PeerInfo> get_peers() {
        return this.peers;
    }

    public ArrayList<PeerManagerInfo> get_managers() {
        return this.managers;
    }

    public SSLContext get_context() {
        return this.context;
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

    public boolean join_network() {
        if(this.other_manager_address == null)
            return true;
        try {
            Message message = new Message("MANAGER_JOIN", -1, null, -1, null, this.address, this.port, null);
            SSLSocketFactory socketfactory = this.context.getSocketFactory();
            SSLSocket socket = (SSLSocket) socketfactory.createSocket(this.other_manager_address, this.other_manager_port);
            socket.getOutputStream().write(message.build());
            byte[] data = new byte[16000];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream stream = socket.getInputStream();
            int nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            byte[] message_data = buffer.toByteArray();
            Message answer = new Message(message_data);
            int count;
            if(answer.get_type().equals("PEER_INFO")) {
                count = answer.get_rep_degree();
            } else return false;
            
            /* Receive peer info */
            String ack_msg = "ACK";
            int n = 0;
            if(count > 0) {
                nRead = stream.read(data, 0, data.length);
                while(nRead > 0) {
                    buffer = new ByteArrayOutputStream();
                    buffer.write(data, 0, nRead);
                    message_data = buffer.toByteArray();
                    Message peer_msg = new Message(message_data);
                    PeerInfo new_peer = new PeerInfo(peer_msg.get_peer_id(), peer_msg.get_port(), peer_msg.get_address());
                    new_peer.set_free_space(peer_msg.get_rep_degree());
                    this.peers.put(peer_msg.get_peer_id(), new_peer);
                    socket.getOutputStream().write(ack_msg.getBytes());
                    if(n == count - 1)
                        break;
                    nRead = stream.read(data, 0, data.length);
                    n++;
                }
            }
            /* End of receive peer info */
            data = new byte[16000];
            buffer = new ByteArrayOutputStream();
            stream = socket.getInputStream();
            nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            message_data = buffer.toByteArray();
            answer = new Message(message_data);
            if(answer.get_type().equals("FILE_INFO")) {
                count = answer.get_rep_degree();
            } else return false;

            /* Receive file info */
            if(count > 0)
            {
                n = 0;
                nRead = stream.read(data, 0, data.length);
                while(nRead > 0) {
                    buffer = new ByteArrayOutputStream();
                    buffer.write(data, 0, nRead);
                    message_data = buffer.toByteArray();
                    Message file_msg = new Message(message_data);
                    this.files.put(file_msg.get_file_id(), file_msg.get_peer_ids());
                    socket.getOutputStream().write(ack_msg.getBytes());
                    if(n == count - 1)
                        break;
                    nRead = stream.read(data, 0, data.length);
                    n++;
                }
            }
            /* End of receive file info */

            
            /* Receive manager info */
            data = new byte[16000];
            buffer = new ByteArrayOutputStream();
            stream = socket.getInputStream();
            nRead = stream.read(data, 0, data.length);
            buffer.write(data, 0, nRead);
            message_data = buffer.toByteArray();
            answer = new Message(message_data);
            if(!answer.get_type().equals("MANAGER_INFO")) return false;

            for(int j = 0; j < answer.get_peers().size(); j++) {
                PeerManagerInfo new_manager = new PeerManagerInfo(answer.get_peers().get(j).get_port(), answer.get_peers().get(j).get_address());
                this.managers.add(new_manager);
            }

            /* End of receive manager info */
            socket.close();
        } catch(Exception ex) {
            System.out.println("Error receiving info from server.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        if(args.length != 1 && args.length != 3) {
            System.out.println("Usage: ");
            System.out.println("  java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password PeerManager <port>");
            System.out.println("  java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password PeerManager <port> <manager_address> <manager_port>");
            return;
        }
        int port = -1;
        String address = null;
        if(args.length == 3) {
            port = Integer.parseInt(args[2]);
            address = args[1];
        }
        new PeerManager(Integer.parseInt(args[0]), address, port);
    }
}