/*
Para testar: 
java Client AsusPC obj_name register AA-11-BB MrBean
java Client AsusPC obj_name lookup AA-11-BB

AsusPC -> hostname
*/
package remote;

import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) throws IOException{
        if(args.length != 4 && args.length != 5) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }
        byte[] sbuf = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).getBytes();
        String hostname = args[0];
        String remote_object_name = args[1];
        String op2;
        if(args.length==4)
            op2=null;
        else
            op2 = args[4];

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            RemoteInterface stub = (RemoteInterface) registry.lookup(remote_object_name);
            String response = stub.checkVehicles(args[2], args[3], op2);
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}