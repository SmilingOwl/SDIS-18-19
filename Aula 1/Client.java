/*
Para testar: 
java Client AsusPC 8888 register AA-11-BB MrBean
java Client AsusPC 8888 lookup AA-11-BB

AsusPC -> hostname
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
        String hostname = args[0];
        InetAddress address = InetAddress.getByName(hostname);
        int port = Integer.parseInt(args[1]);
        try (DatagramSocket clientSocket = new DatagramSocket()){
            DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);
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
    }
}