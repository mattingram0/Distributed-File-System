import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Server3 implements ServerInterface {

    public int numFiles() {
        return 0;
    }

    public void ping() {
    }

    public int checkSpace() throws RemoteException {
        return 0;
    }

    ;

    public ArrayList<String> list() throws RemoteException {
        return new ArrayList<String>();
    }

    ;

    public void download() throws RemoteException {
    }

    ;

    public void upload() throws RemoteException {
    }

    ;

    public void delete(String filename) throws RemoteException {
    }

    public static void main(String args[]) {

        try {
            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            Server3 server3 = new Server3();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server3, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048);
            registry.rebind("Server3", stub);

            System.out.println("[+] Server 3 ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Server 3: " + e.toString());
            e.printStackTrace();
        }
    }
}
