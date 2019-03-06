import java.net.*;
import java.io.*;
import java.util.*;

public class TestApp {
    public static void main(String[] args) {
        if(args.length < 2 && args.length >4) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }
        
        switch(args[1])
        {
            case "BACKUP":
                checkBackup(args);
                break;
            case "RESTORE":
                checkRestore(args);
                break;
            case "DELETE":
                checkDelete(args);
                break;
            case "RECLAIM":
                checkReclaim(args);
                break;
            case "STATE":
                checkState(args);
                break;
            default:
                System.out.println("ERROR: Second argument, relative to the sub-protocol, not recognized.");
                return;
        }
    }

    public static int checkBackup(String[] args) {
        if(args.length != 4) {
            System.out.println("Error: Wrong number of arguments");
            return -1;
        }
        return 0;
    }

    public static int checkRestore(String[] args) {
        if(args.length != 3) {
            System.out.println("Error: Wrong number of arguments");
            return -1;
        }
        return 0;
    }

    public static int checkDelete(String[] args) {
        if(args.length != 3) {
            System.out.println("Error: Wrong number of arguments");
            return -1;
        }
        return 0;
    }

    public static int checkReclaim(String[] args) {
        if(args.length != 3) {
            System.out.println("Error: Wrong number of arguments");
            return -1;
        }
        return 0;
    }

    public static int checkState(String[] args) {
        if(args.length != 2) {
            System.out.println("Error: Wrong number of arguments");
            return -1;
        }
        return 0;
    }
}