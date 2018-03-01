import java.io.*;
import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class FrontEnd implements FrontEndInterface {

    ServerInterface server1;
    ServerInterface server2;
    ServerInterface server3;

    File duplicates;
    File fileList1;
    File fileList2;
    File fileList3;


    public FrontEnd() {
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

            //Create duplicates file if it doesn't exist
            if (!(frontEnd.duplicates = new File("duplicates.txt")).exists()) {
                frontEnd.duplicates.createNewFile();
            }
            if (!(frontEnd.fileList1 = new File("server1.txt")).exists()) {
                frontEnd.fileList1.createNewFile();
            }
            if (!(frontEnd.fileList2 = new File("server2.txt")).exists()) {
                frontEnd.fileList2.createNewFile();
            }
            if (!(frontEnd.fileList3 = new File("server3.txt")).exists()) {
                frontEnd.fileList3.createNewFile();
            }

            System.out.println("[+] Front End Server ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Front End Server: " + e.toString());
            e.printStackTrace();
        }
    }

    public void upload() {
        ArrayList<String> filesInList;
        ArrayList<String> duplicateFiles = new ArrayList<>();
        String fileInput;
        ArrayList<ServerInterface> servers;
        ArrayList<String> filesOnServer;
        BufferedReader reader;
        ServerInterface emptiestServer = null;
        int numFiles;
        int minimum = Integer.MAX_VALUE;

        servers = checkStatus();

        if (servers.size() == 0) {
            // handle no servers available
        } else {
            for (ServerInterface server : servers) {
                try {
                    //Find the emptiest server
                    numFiles = server.numFiles();

                    if (numFiles < minimum) {
                        minimum = numFiles;
                        emptiestServer = server;
                    }

                    filesOnServer = server.list();

                    try {
                        reader = new BufferedReader(new FileReader(duplicates));

                        while ((fileInput = reader.readLine()) != null) {
                            duplicateFiles.add(fileInput);
                        }

                        while ((fileInput = reader.readLine()) != null) {
                            duplicateFiles.add(fileInput);
                        }
                    } catch (IOException e) {
                        e.printStackTrace(); //TODO proper exception handling
                    }


                } catch (RemoteException e) {
                    System.out.println("[-] " + server.toString() + "failed during list operation");
                }
            }
        }

        if (emptiestServer == null) {
            // handle all servers failed to respond to list operation
        } else {
            //create socket connection to handle the upload, check the upload doesn't exist on any of them
        }
    }

    public String list() {
        ArrayList<ServerInterface> servers;
        StringBuilder listing = new StringBuilder();
        int counter = 0;

        servers = checkStatus();

        if (servers.size() == 0) {
            return "0"; //No servers available TODO: handle this on client side
        } else {
            for (ServerInterface server : servers) {
                try {
                    listing.append(server.list());
                } catch (RemoteException e) {
                    System.out.println("[-] " + server.toString() + "failed during list operation");
                    counter++;
                }
            }
        }

        if (counter == servers.size()) {
            return "0"; //All servers went down during list operation
        } else {
            return listing.toString();
        }
    }

    public ArrayList<ServerInterface> checkStatus() {
        ServerInterface allServers[] = {server1, server2, server3};
        File allFileLists[] = {fileList1, fileList2, fileList3};
        ArrayList<ServerInterface> upServers = new ArrayList<>();
        ArrayList<File> upFileLists = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            try {
                allServers[i].ping();
                upServers.add(allServers[i]);
                upFileLists.add(allFileLists[i]);
            } catch (RemoteException e) {
                System.out.println("[-] " + server1.toString() + "not available");
            }
        }
        return upServers;
    }
}

/* We either monitor using periodic UDP calls, if one fails we set a flag not to use it, keep sending out calls to it though when we get a succesful call.*/