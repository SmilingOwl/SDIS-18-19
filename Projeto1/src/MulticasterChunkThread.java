import java.net.*;
import java.io.*;
import java.util.*;

public class MulticasterChunkThread implements Runnable {
    private InetAddress mdr_address;
    private int mdr_port;
    private byte[] message;
    private int chunk_no;
    private String file_id;
    private int port;
    private InetAddress address;
    private Peer peer;
    private String version;

    MulticasterChunkThread(InetAddress mdr_address, int mdr_port, Peer peer, byte[] message, 
            String file_id, int chunk_no, int port, String address, String version){
        this.mdr_address = mdr_address;
        this.mdr_port = mdr_port;
        this.message = message;
        this.peer = peer;
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.port = port;
        try {
            this.address = InetAddress.getByName(address);
        } catch(Exception ex) {
            System.out.println("Error inet address get name");
        }
        this.version = version;
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
        if(this.version.equals("1.0")){
            try (DatagramSocket socket = new DatagramSocket()) {
                DatagramPacket sendPort = new DatagramPacket(this.message, this.message.length, this.mdr_address, this.mdr_port);
                socket.send(sendPort);
            } catch(IOException ex) {
                System.out.println("Error sending packet in Chunk.");
            }
        } else if (this.version.equals("2.0")){
            try {
                Socket socket = new Socket(address, port);
                socket.getOutputStream().write(this.message);
                socket.close();
                Message to_send = new Message("SENTCHUNK", "2.0", this.peer.get_id(), this.file_id, this.chunk_no, 0, null);
                this.peer.sendMessageMC(to_send.build());
            } catch(Exception ex) {
                System.out.println("Error Socket");
            }
        } else
            System.out.println("Error on MulticasterChunkThread. Unrecognized version.");
    }
}