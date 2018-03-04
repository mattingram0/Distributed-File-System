import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
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

    public ArrayList<String> list() {
        ArrayList<String> listing = new ArrayList<>();
        File[] listOfFiles = new File("files/").listFiles();

        for (File file : listOfFiles) {
            listing.add(file.getName());
        }

        return listing;
    }

    public void download() throws RemoteException {
    }

    public void upload() throws RemoteException {
    }

    public void delete(String filename) throws RemoteException {
        if (new File("files/" + filename).delete()) {
            System.out.println("[+] " + filename + " deleted successfully from Server1");
        } else {
            System.out.println("[-] Unable to delete " + filename + " file from Server1");
        }
    }

    public static void main(String args[]) {

        try {
            //Set policy file
            System.setProperty("java.security.policy", "server.policy");

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
