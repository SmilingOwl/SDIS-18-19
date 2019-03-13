import java.net.*;
import java.io.*;
import java.util.*;

public class MCThread implements Runnable {
    MulticastSocket mc_socket;
    private static InetAddress mc_address;
    private static int mc_port;
    Peer peer;
    MCThread(InetAddress mc_address, int mc_port, Peer peer) {
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

    public void run(){
        try{
            while(true){
                byte[] buf = new byte[512];
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                mc_socket.receive(msgPacket);
                String data = new String(msgPacket.getData());
                System.out.println(data);
                this.peer.receiveMessageMC(data);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = message.getBytes();
            DatagramPacket sendPort = new DatagramPacket(buf, buf.length, this.mc_address, this.mc_port);
            socket.send(sendPort);
            System.out.println("multicast message sent");
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}