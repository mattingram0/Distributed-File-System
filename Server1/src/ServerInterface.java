import java.io.File;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public interface ServerInterface extends Remote {

    int numFiles() throws RemoteException;

    void ping() throws RemoteException;

    void delete(String filename) throws RemoteException;

    int checkSpace() throws RemoteException;

    ArrayList<String> list() throws RemoteException;

    void download() throws RemoteException;

    boolean upload(int port) throws RemoteException;

    String getIpAddress() throws RemoteException;
}
