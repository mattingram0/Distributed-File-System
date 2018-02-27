import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server3 implements ServerInterface {
    public static void main(String args[]) {

        try {
            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            Server1 server3 = new Server1();
            FrontEndInterface stub = (FrontEndInterface) UnicastRemoteObject.exportObject(server3, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048);
            registry.rebind("Server3", stub);

            System.out.println("[+] Server 3 ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Server 3: " + e.toString());
            e.printStackTrace();
        }
    }
}
