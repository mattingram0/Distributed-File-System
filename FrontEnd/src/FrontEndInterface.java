import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Set;

interface FrontEndInterface extends Remote {
    Set<String> list() throws RemoteException;

    boolean upload(int port, String filename, boolean reliable) throws RemoteException;

    void push(String filename, boolean exists, boolean reliable) throws RemoteException;

    boolean delete(String filename) throws RemoteException, FileNotFoundException;

    boolean download(int port, String filename) throws RemoteException, FileNotFoundException;

}
