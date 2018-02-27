import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server1 implements ServerInterface {
    public static void main(String args[]) {

        try {
            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            Server1 server1 = new Server1();
            FrontEndInterface stub = (FrontEndInterface) UnicastRemoteObject.exportObject(server1, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048);
            registry.rebind("Server1", stub);

            System.out.println("[+] Server 1 ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Server 1: " + e.toString());
            e.printStackTrace();
        }
    }
}
