import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server2 implements ServerInterface {

    public void ping() {
    }

    public static void main(String args[]) {

        try {
            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            Server1 server2 = new Server1();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server2, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048);
            registry.rebind("Server2", stub);

            System.out.println("[+] Server 2 ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Server 2: " + e.toString());
            e.printStackTrace();
        }
    }
}
