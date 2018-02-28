import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public interface ServerInterface extends Remote {

    void ping() throws RemoteException;

    int checkSpace() throws RemoteException;

    String list() throws RemoteException;

    void download() throws RemoteException;

    void upload() throws RemoteException;
}
