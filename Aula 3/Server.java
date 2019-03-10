/*
Para compilar e correr:
javac -d . *.java
start rmiregistry
java remote/Server obj_name
*/

package remote;

import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server implements RemoteInterface{
    Map<String, String> vehicles;
    public Server() {
        this.vehicles = new HashMap<>();
    }

    public String checkVehicles(String oper, String op1, String op2) {
        System.out.println("entered");
        String message="";
        if(oper.equals("register"))
        {
            if(this.vehicles.get(op1) == null) {
                String name = op2.replaceAll("[^a-zA-Z0-9-]", "");
                vehicles.put(op1, name);
                message = Integer.toString(this.vehicles.size());
            } else {
                message = "-1";
            }
        } else if(oper.equals("lookup")) {
            String regist = op1.replaceAll("[^a-zA-Z0-9-]", "");
            String owner_name = this.vehicles.get(regist);
            if(owner_name == null) {
                message = "-1";
            } else {
                message = regist + " " + owner_name;
            }
        } else {
            message = "-1";
        }
        return message;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Error: Wrong number of arguments");
            return;
        }
        String remote_object_name = args[0];

        try {
            Server obj = new Server();
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(args[0], stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

    }
}