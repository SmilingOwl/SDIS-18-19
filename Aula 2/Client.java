/*
Para testar: java Client 224.0.0.3 6789 register 88-19-AB Michael
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
    public static void main(String[] args) throws IOException{
        if(args.length != 4 && args.length != 5) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }
        byte[] sbuf = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).getBytes();
        InetAddress address = InetAddress.getByName(args[0]);
        String mcast_addr = args[0];
        int mcast_port = Integer.parseInt(args[1]);

        try(MulticastSocket mcast_socket = new MulticastSocket(mcast_port)) {
            mcast_socket.joinGroup(address);
            byte[] buf = new byte[255];
            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
            mcast_socket.receive(msgPacket);
            String aux = new String(msgPacket.getData()); //message received: port-address
            String info = aux.replaceAll("[^0-9-.]", "");
            String[] aux2 = info.split("-");
            int port = Integer.parseInt(aux2[0]);
            InetAddress service_address = InetAddress.getByName(aux2[1]);
            System.out.println("Port: " + port);
            System.out.println("Server's address: " + aux2[1]);
            
            try (DatagramSocket clientSocket = new DatagramSocket()){
                DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, service_address, port);
                clientSocket.send(packet);
                System.out.println("Sent packet: " + String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                System.out.println("To Port: " + port);

                byte[] rbuf = new byte[sbuf.length];
                packet = new DatagramPacket(rbuf, rbuf.length);
                System.out.println("Waiting for packet...");
                clientSocket.receive(packet);
                String received = new String(packet.getData());
                System.out.println("Message Received: " + received);
                clientSocket.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }

    }
}