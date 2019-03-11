import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{
    String backup_file(String file_name, int rep_degree) throws RemoteException;
    String restore_file(String file_name) throws RemoteException;
    String delete_file(String file_name) throws RemoteException;
    String reclaim(int max_ammount) throws RemoteException;
    String state() throws RemoteException;
}