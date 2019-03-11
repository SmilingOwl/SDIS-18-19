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

        String remote_object_name = args[0];

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            RemoteInterface stub = (RemoteInterface) registry.lookup(remote_object_name);
            switch(args[1])
            {
                case "BACKUP":
                    checkBackup(stub, args[2], args[3]);
                    break;
                case "RESTORE":
                    checkRestore(stub, args[2]);
                    break;
                case "DELETE":
                    checkDelete(stub, args[2]);
                    break;
                case "RECLAIM":
                    checkReclaim(stub, args[2]);
                    break;
                case "STATE":
                    checkState(stub);
                    break;
                default:
                    System.out.println("ERROR: Second argument, relative to the sub-protocol, not recognized.");
                    return;
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static int checkBackup(RemoteInterface stub, String file_path, String rep_deg) {
        int rep_degree = Integer.parseInt(rep_deg);
        stub.backup_file(file_path, rep_deg);
        return 0;
    }

    public static int checkRestore(RemoteInterface stub, String file_path) {
        stub.restore_file(file_path, rep_deg);
        return 0;
    }

    public static int checkDelete(RemoteInterface stub, String file_path) {
        stub.delete_file(file_path, rep_deg);
        return 0;
    }

    public static int checkReclaim(RemoteInterface stub, String max_amm) {
        int max_ammount = Integer.parseInt(max_amm);
        stub.reclaim(max_ammount);
        return 0;
    }

    public static int checkState(RemoteInterface stub) {
        stub.state();
        return 0;
    }
}