import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class FrontEnd implements FrontEndInterface {

    ServerInterface server1;
    ServerInterface server2;
    ServerInterface server3;


    public FrontEnd() {
    }

    public String sayHello() {
        return "Sucessful remote invocation!";
    }

    public static void main(String args[]) {

        try {
            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            FrontEnd frontEnd = new FrontEnd();
            FrontEndInterface stub = (FrontEndInterface) UnicastRemoteObject.exportObject(frontEnd, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048);
            registry.rebind("FrontEnd", stub);

            //Get the server stubs from the registry to be used by the master
            frontEnd.server1 = (ServerInterface) registry.lookup("Server1");
            frontEnd.server2 = (ServerInterface) registry.lookup("Server2");
            frontEnd.server3 = (ServerInterface) registry.lookup("Server3");

            System.out.println("[+] Front End Server ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Front End Server: " + e.toString());
            e.printStackTrace();
        }
    }

}
