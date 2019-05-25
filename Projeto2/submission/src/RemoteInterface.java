import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{
    public String backup_file(String file_name, int rep_degree) throws RemoteException;
    public String restore_file(String file_name) throws RemoteException;
    public String delete_file(String file_name) throws RemoteException;
    public String reclaim(int max_ammount) throws RemoteException;
}