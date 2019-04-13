import java.net.*;
import java.io.*;


public class MCThread implements Runnable {
    MulticastSocket mc_socket;
    private InetAddress mc_address;
    private int mc_port;
    Peer peer;

    MCThread(InetAddress mc_address, int mc_port, Peer peer) {
        this.mc_address = mc_address;
        this.mc_port = mc_port;
        this.peer = peer;
        try {
            this.mc_socket = new MulticastSocket(this.mc_port);
            this.mc_socket.joinGroup(this.mc_address);
        } catch(IOException ex) {
            System.out.println("Error joining mcast in MCThread");
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
            System.out.println("Error receiving packet in MCThread.");
        }
    }

    public void sendMessage(byte[] buf) {
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket sendPort = new DatagramPacket(buf, buf.length, this.mc_address, this.mc_port);
            socket.send(sendPort);
        } catch(IOException ex) {
            System.out.println("Error sending packet in MCThread.");
        }
    }
}