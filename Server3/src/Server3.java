import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Server3 implements ServerInterface {

    String ip; //IP address this server is running on, to allow for socket creation

    @Override
    public String getIpAddress() {
        return this.ip;
    }

    public int numFiles() {
        return 0;
    }

    public void ping() {
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

    public boolean upload(int port) throws RemoteException {
        return false;
    }


    public void delete(String filename) throws RemoteException {
        if (new File("files/" + filename).delete()) {
            System.out.println("[+] " + filename + " deleted successfully from Server3");
        } else {
            System.out.println("[-] Unable to delete " + filename + " file from Server3");
        }
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
