import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.Arrays;

public class MDBThread implements Runnable {
    MulticastSocket mdb_socket;
    private InetAddress mdb_address;
    private int mdb_port;
    Peer peer;
    MDBThread(InetAddress mdb_address, int mdb_port, Peer peer) {
        this.mdb_address = mdb_address;
        this.mdb_port = mdb_port;
        this.peer = peer;
        try {
            this.mdb_socket = new MulticastSocket(this.mdb_port);
            this.mdb_socket.joinGroup(this.mdb_address);
        } catch(IOException ex) {
           System.out.println("Error joining mcast in MDBThread.");
        }
    }
    
    @Override   
    public void run(){
        try{
            while(true){
                byte[] buf = new byte[65000];
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                mdb_socket.receive(msgPacket);
                byte[] buffer = Arrays.copyOf(buf, msgPacket.getLength());
                Random rand = new Random();
                int random_delay = rand.nextInt(401);
                this.peer.get_thread_executor().schedule(new ReceiveMessageMDB(buffer, this.peer), random_delay, TimeUnit.MILLISECONDS);
            }
        } catch(IOException ex) {
            System.out.println("Error receiving packet in MDBThread.");
        }
    }
}