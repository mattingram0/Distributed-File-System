import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FrontEndInterface extends Remote {
    String list() throws RemoteException;

}
