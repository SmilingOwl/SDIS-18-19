import java.net.*;
import java.io.*;
import java.util.*;

public class MulticasterChunkThread implements Runnable {
    private InetAddress mdr_address;
    private int mdr_port;
    private byte[] message;
    private int chunk_no;
    private String file_id;
    private Peer peer;

    MulticasterChunkThread(InetAddress mdr_address, int mdr_port, Peer peer, byte[] message, String file_id, int chunk_no) {
        this.mdr_address = mdr_address;
        this.mdr_port = mdr_port;
        this.message = message;
        this.peer = peer;
        this.file_id = file_id;
        this.chunk_no = chunk_no;
    }

    @Override
    public void run(){
        ArrayList<String> chunks_not_to_send = this.peer.get_myChunksNotToSend();
        for(int i = 0; i < chunks_not_to_send.size(); i++) {
            if(chunks_not_to_send.get(i).equals(this.file_id + ":" + this.chunk_no)) {
                chunks_not_to_send.remove(i);
                return;
            }
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket sendPort = new DatagramPacket(this.message, this.message.length, this.mdr_address, this.mdr_port);
            socket.send(sendPort);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}