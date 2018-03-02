import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FrontEndInterface extends Remote {
    ArrayList<ArrayList<String>> list() throws RemoteException;

}
