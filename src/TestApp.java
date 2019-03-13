/*
java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2> 
<peer_ap> -> hostname:remote_object_name
*/

import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {
    public static void main(String[] args) {
        if(args.length < 2 && args.length >4) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }
        String[] peer_ap = args[0].split(":");
        String hostname = peer_ap[0];
        String remote_object_name = peer_ap[1];
        String file_path = args[2];
        String response;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            RemoteInterface stub = (RemoteInterface) registry.lookup(remote_object_name);
            switch(args[1])
            {
                case "BACKUP":
                    int rep_degree = Integer.parseInt(args[3]);
                    response = stub.backup_file(file_path, rep_degree);
                    break;
                case "RESTORE":
                    response = stub.restore_file(file_path);
                    break;
                case "DELETE":
                    response = stub.delete_file(file_path);
                    break;
                case "RECLAIM":
                    int max_ammount = Integer.parseInt(file_path);
                    response = stub.reclaim(max_ammount);
                    break;
                case "STATE":
                    response = stub.state();
                    break;
                default:
                    System.out.println("ERROR: Second argument, relative to the sub-protocol, not recognized.");
                    return;
            }
            System.out.println(response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}