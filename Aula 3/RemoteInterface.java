package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{
    String checkVehicles(String oper, String op1, String op2) throws RemoteException;
}