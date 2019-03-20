import java.net.*;
import java.io.*;
import java.util.*;

public class MRThread implements Runnable {
    MulticastSocket mc_socket;
    private static InetAddress mc_address;
    private static int mc_port;
    Peer peer;

    MRThread(InetAddress mc_address, int mc_port, Peer peer) {
        this.mc_address = mc_address;
        this.mc_port = mc_port;
        this.mc_socket = mc_socket;
        this.peer = peer;
        try {
            this.mc_socket = new MulticastSocket(this.mc_port);
            this.mc_socket.joinGroup(this.mc_address);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run(){
        try{
            while(true){
                byte[] buf = new byte[65000];
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                mc_socket.receive(msgPacket);
                this.peer.get_thread_executor().execute(new ReceiveMessageMC(msgPacket.getData(), this.peer));
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(byte[] buf, int random_delay) {
        try {
            Thread.sleep(random_delay);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket sendPort = new DatagramPacket(buf, buf.length, this.mc_address, this.mc_port);
            socket.send(sendPort);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}