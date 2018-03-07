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

    private String ip; //IP address this server is running on, to allow for socket creation TODO: fix/set this, commandline?

    @Override
    public String getIpAddress() {
        return this.ip;
    }

    public int numFiles() {
        System.out.print("Number of Files on Server 1: ");
        System.out.println(list().size());
        return list().size();
    }

    public void ping() {
    }

    public ArrayList<String> list() {
        ArrayList<String> listing = new ArrayList<>();
        File[] listOfFiles = new File("files/").listFiles();

        for (File file : listOfFiles) {
            listing.add(file.getName());
        }

        return listing;
    }

    public boolean download(int port, String filename) {
        TransferHelper helper = new TransferHelper(port, "D", filename);
        Thread thread = new Thread(helper);
        thread.start();
        System.out.println("download listener started on server");
        return true;
    }

    public boolean receive(int port) {
        TransferHelper helper = new TransferHelper(port, "R");
        Thread thread = new Thread(helper);
        thread.start();
        return true;
    }

    public void delete(String filename) {
        if (new File("files/" + filename).delete()) {
            System.out.println("[+] " + filename + " deleted successfully from Server1");
        } else {
            System.out.println("[-] Unable to delete " + filename + " file from Server1");
        }
    }

    public static void main(String args[]) {

        try {

            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            Server1 server1 = new Server1();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server1, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048); //TODO add this to client side - change policy files
            registry.rebind("Server1", stub);

            System.out.println("[+] Server 1 ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Server 1: " + e.toString());
            e.printStackTrace();
        }
    }
}