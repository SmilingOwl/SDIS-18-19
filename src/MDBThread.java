import java.net.*;
import java.io.*;
import java.util.*;

public class MDBThread implements Runnable {
    MulticastSocket mdb_socket;
    private static InetAddress mdb_address;
    private static int mdb_port;
    Peer peer;
    MDBThread(InetAddress mdb_address, int mdb_port, Peer peer) {
        this.mdb_address = mdb_address;
        this.mdb_port = mdb_port;
        this.mdb_socket = mdb_socket;
        this.peer = peer;
        try {
            this.mdb_socket = new MulticastSocket(this.mdb_port);
            this.mdb_socket.joinGroup(this.mdb_address);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run(){
        try{
            while(true){
                byte[] buf = new byte[512];
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                mdb_socket.receive(msgPacket);
                String data = new String(msgPacket.getData());
                System.out.println(data);
                this.peer.receiveMessageMDB(data);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}