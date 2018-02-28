import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Server1 implements ServerInterface {

    public void ping() {
    }

    public int checkSpace() throws RemoteException {
        return 0;
    }

    ;

    public String list() throws RemoteException {
        StringBuilder fileList = new StringBuilder();
        StringBuilder directoryList = new StringBuilder();

        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                fileList.append(file.getName() + "\n");
            }
            if (file.isDirectory()) {
                directoryList.append("[D] " + file.getName() + "\n");
            }
        }

        return directoryList.toString() + fileList.toString();
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
