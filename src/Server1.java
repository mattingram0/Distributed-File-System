import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Server1 implements ServerInterface {

    public int numFiles() {
        return 0;
    }

    public void ping() throws RemoteException {
    }

    public int checkSpace() throws RemoteException {
        return 0;
    }

    public ArrayList<String> list() throws RemoteException {
        ArrayList<String> files = new ArrayList<>();
        ArrayList<String> directories = new ArrayList<>();
        ArrayList<String> listing;

        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                files.add(file.getName());
            }
            if (file.isDirectory()) {
                directories.add("[D] " + file.getName());
            }
        }

        listing = new ArrayList<>(directories);
        listing.addAll(files);

        return listing;
    }

    public void download() throws RemoteException {
    }

    ;

    public void upload() throws RemoteException {
    }

    ;

    public static void main(String args[]) {

        try {
            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            Server1 server1 = new Server1();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server1, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048);
            registry.rebind("Server1", stub);


            System.out.println("[+] Server 1 ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Server 1: " + e.toString());
            e.printStackTrace();
        }
    }
}
