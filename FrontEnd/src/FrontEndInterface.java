import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Set;

public interface FrontEndInterface extends Remote {
    Set<String> list() throws RemoteException;

    boolean upload(int port, String filename, boolean reliable) throws RemoteException;

    void push(String filename, boolean exists, boolean reliable) throws RemoteException;

}
