import java.net.*;
import java.io.*;
import java.util.*;

public class MulticasterChunkThread implements Runnable {
    private InetAddress mdr_address;
    private int mdr_port;
    private byte[] message;

    MulticasterChunkThread(InetAddress mdr_address, int mdr_port, byte[] message) {
        this.mdr_address = mdr_address;
        this.mdr_port = mdr_port;
        this.message = message;
    }

    @Override
    public void run(){
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket sendPort = new DatagramPacket(this.message, this.message.length, this.mdr_address, this.mdr_port);
            socket.send(sendPort);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}