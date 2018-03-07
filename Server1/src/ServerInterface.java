import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

interface ServerInterface extends Remote {

    int numFiles() throws RemoteException;

    void ping() throws RemoteException;

    void delete(String filename) throws RemoteException;

    ArrayList<String> list() throws RemoteException;

    boolean download(int port) throws RemoteException;

    boolean receive(int port) throws RemoteException;

    String getIpAddress() throws RemoteException;
}
