import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class MulticasterPutChunkThread implements Runnable {
    private InetAddress mc_address;
    private int mc_port;
    private byte[] message;
    private int sent_messages_no;
    private Chunk chunk;
    private Peer peer;

    MulticasterPutChunkThread(InetAddress mc_address, int mc_port, byte[] message, Chunk chunk, Peer peer) {
        this.mc_address = mc_address;
        this.mc_port = mc_port;
        this.message = message;
        this.sent_messages_no = 0;
        this.chunk = chunk;
        this.peer = peer;
    }

    @Override
    public void run(){
        ArrayList<String> chunks_not_to_send = this.peer.get_myChunksNotToSend();
        for(int i = 0; i < chunks_not_to_send.size(); i++) {
            if(chunks_not_to_send.get(i).equals(this.chunk.get_file_id() + ":" + this.chunk.get_chunk_no())) {
                chunks_not_to_send.remove(i);
                return;
            }
        }
        String key = this.chunk.get_file_id() + ":" + this.chunk.get_chunk_no();
        int occurrences = 0;
        if(this.peer.get_chunk_occurrences().get(key) != null)
            occurrences = this.peer.get_chunk_occurrences().get(key).size();
        
        while(occurrences < this.chunk.get_rep_degree() && this.sent_messages_no < 5) {
            this.send_message();
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.out.println("Error on sleep.");
            }
            if(this.peer.get_chunk_occurrences().get(key) != null)
                occurrences = this.peer.get_chunk_occurrences().get(key).size();
        }
    }

    private void send_message() {
        this.sent_messages_no++;
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket sendPort = new DatagramPacket(this.message, this.message.length, this.mc_address, this.mc_port);
            socket.send(sendPort);
        } catch(IOException ex) {
            System.out.println("Error sending packet in PUTCHUNK.");
        }
    }
}