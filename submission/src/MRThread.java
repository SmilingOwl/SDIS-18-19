import java.net.*;
import java.io.*;
import java.util.Arrays;

public class MRThread implements Runnable {
    MulticastSocket mdr_socket;
    private InetAddress mdr_address;
    private int mdr_port;
    Peer peer;

    MRThread(InetAddress mdr_address, int mdr_port, Peer peer) {
        this.mdr_address = mdr_address;
        this.mdr_port = mdr_port;
        this.peer = peer;
        try {
            this.mdr_socket = new MulticastSocket(this.mdr_port);
            this.mdr_socket.joinGroup(this.mdr_address);
        } catch(IOException ex) {
            System.out.println("Error joining mcast in MRThread.");
        }
    }

    @Override   
    public void run(){
        try{
            while(true){
                byte[] buf = new byte[65000];
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                mdr_socket.receive(msgPacket);
                byte[] buffer = Arrays.copyOf(buf, msgPacket.getLength());
                this.peer.get_thread_executor().execute(new ReceiveMessageMDR(buffer, this.peer));
            }
        } catch(IOException ex) {
            System.out.println("Error receiving packet in MRThread.");
        }
    }
}