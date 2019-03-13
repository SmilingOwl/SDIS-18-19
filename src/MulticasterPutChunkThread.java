import java.net.*;
import java.io.*;
import java.util.*;

public class MulticasterPutChunkThread implements Runnable {
    MulticastSocket mc_socket;
    private InetAddress mc_address;
    private int mc_port;
    private String message;

    MulticasterPutChunkThread(InetAddress mc_address, int mc_port, String message) {
        this.mc_address = mc_address;
        this.mc_port = mc_port;
        this.mc_socket = mc_socket;
        this.message = message;
    }

    public void run(){
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = this.message.getBytes();
            DatagramPacket sendPort = new DatagramPacket(buf, buf.length, this.mc_address, this.mc_port);
            socket.send(sendPort);
            System.out.println("MulticasterPutChunkThread: multicast message sent");
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}