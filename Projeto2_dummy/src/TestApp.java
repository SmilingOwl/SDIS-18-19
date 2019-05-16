/*
java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2> 
<peer_ap> -> hostname:remote_object_name
*/

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {
    public static void main(String[] args) {
        if(args.length < 2 || args.length > 4) {
            System.out.println("Error: Wrong number of arguments");
            System.out.println("Usage: java TestApp <hostname>:<remote_object_name> <protocol> <opnd_1> <opnd_2>");
            return;
        }
        String[] peer_ap = args[0].split(":");
        String hostname = peer_ap[0];
        String remote_object_name = peer_ap[1];
        String file_path = "";
        if(args.length > 2)
            file_path = args[2];
        String response;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            RemoteInterface stub = (RemoteInterface) registry.lookup(remote_object_name);
            if(args[1].equals("BACKUP")){
                int rep_degree = Integer.parseInt(args[3]);
                response = stub.backup_file(file_path, rep_degree);

            } else if(args[1].equals("RESTORE")){
                response = stub.restore_file(file_path);

            } else if(args[1].equals("DELETE")){
                response = stub.delete_file(file_path);

            } else {
                System.out.println("Error: Second argument, relative to the sub-protocol, not recognized.");
                return;
            }
            System.out.println(response);
        } catch (Exception e) {
            System.err.println("Something went wrong.");
        }
    }
}