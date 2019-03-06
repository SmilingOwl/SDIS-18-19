/*
Para testar: java Server 8888
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    public static void main(String[] args) throws IOException{
        if(args.length != 1) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }
        Map<String, String> vehicles = new HashMap<String, String>();
        int srvc_port = Integer.parseInt(args[0]);

        try(DatagramSocket socket = new DatagramSocket(srvc_port)) {      
            System.out.println("Opened Socket in port: " + srvc_port);      
            while(true) {
                int length = 512;
                byte[] buf = new byte[length];
                DatagramPacket spacket = new DatagramPacket(buf, length);
                System.out.println("Waiting for packet...");
                socket.receive(spacket);
                String request = new String(spacket.getData());
                InetAddress client_address = spacket.getAddress();
                int client_port = spacket.getPort();
                System.out.println("Received Request: " + request);
                String[] separated = request.split(" ");
                String message;
                if(separated[0].equals("register"))
                {
                    if(vehicles.get(separated[1]) == null) {
                        String name = separated[2].replaceAll("[^a-zA-Z0-9-]", "");
                        vehicles.put(separated[1], name);
                        message = Integer.toString(vehicles.size());
                    } else {
                        message = "-1";
                    }
                } else if(separated[0].equals("lookup")) {
                    String regist = separated[1].replaceAll("[^a-zA-Z0-9-]", "");
                    String owner_name = vehicles.get(regist);
                    if(owner_name == null) {
                        message = "-1";
                    } else {
                        message = regist + " " + owner_name;
                    }
                } else {
                    message = "-1";
                }
                System.out.println("Message: " + message);
                byte[] rbuf = message.getBytes();
                DatagramPacket rpacket = new DatagramPacket(rbuf, rbuf.length, client_address, client_port);
                socket.send(rpacket);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}