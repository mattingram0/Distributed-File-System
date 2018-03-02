import java.io.*;
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

    ArrayList<ServerInterface> upServers;
    boolean changedState = false; //Set to true when a server fails or comes back, to recheck files

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

            //Create text documents holding the files that should exist on each server, and a separate doc for duplicates
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
        ServerInterface server; //A single server
        ServerInterface emptiestServer = null;
        ServerInterface listOfServers[];
        listOfServers = checkStatus().getServers().toArray(new ServerInterface[0]);
        int numFiles;
        int minimum = Integer.MAX_VALUE;


        if (listOfServers.length == 0) {
            // handle no servers available
        } else {

            if (changedState = true) {
                updateServers();
            } else {

            }
        }

        //Find the emptiest server
        numFiles = server.numFiles();

        if (numFiles < minimum) {
            minimum = numFiles;
            emptiestServer = server;
        }

        if (emptiestServer == null) {
            // handle all servers failed to respond to list operation
        } else {
            //create socket connection to handle the upload, check the upload doesn't exist on any of them
        }
    }

    public void updateServers() {
        ServerInterface server; //A single server
        ServerInterface listOfServers[]; //An array of available servers
        File fileList; //A text document containing the files a single server should have
        File listOfFileLists[]; //An array containing the fileList of each server
        ArrayList<String> correctFilesOnServer = new ArrayList<>(); //A list containing the files that should be on the server
        ArrayList<ArrayList<String>> listOfCorrectFilesOnServer = new ArrayList<>(); //An arraylist containing the list of files that should be on the server, for each server
        ArrayList<String> actualFilesOnServer = new ArrayList<>(); //A list containing the files actually on the server
        ArrayList<ArrayList<String>> listOfActualFilesOnServer = new ArrayList<>(); //An arraylist containing the list of files actually on the server, for each server
        ArrayList<String> duplicateFiles = new ArrayList<>();
        String line;
        BufferedReader reader;

        //Get the list of available servers, and their filelist text documents
        listOfServers = checkStatus().getServers().toArray(new ServerInterface[0]);
        listOfFileLists = checkStatus().getFileLists().toArray(new File[0]);

        //Read in the duplicate files
        try {
            reader = new BufferedReader(new FileReader(duplicates));

            while ((line = reader.readLine()) != null) {
                duplicateFiles.add(line);
            }
        } catch (IOException e) {
            //TODO
        }

        //Get the list of files for each server, add each list to a containing arraylist
        for (int i = 0; i < listOfServers.length; i++) {
            server = listOfServers[i];
            fileList = listOfFileLists[i];

            try {
                reader = new BufferedReader(new FileReader(fileList));
                while ((line = reader.readLine()) != null) {
                    correctFilesOnServer.add(line);
                }

                listOfCorrectFilesOnServer.add(correctFilesOnServer);

            } catch (IOException e) {
                e.printStackTrace(); //TODO proper exception handling
            }
        }
    }

    public ArrayList<ArrayList<String>> list() {
        ServerInterface listOfServers[];
        ServerInterface server;
        File listOfFileLists[];
        File fileList;
        String line;
        BufferedReader reader;

        ArrayList<ServerInterface> servers;
        ArrayList<String> filesOnServer = new ArrayList<>();
        ArrayList<ArrayList<String>> listing = new ArrayList<>();

        listOfServers = checkStatus().getServers().toArray(new ServerInterface[0]);
        listOfFileLists = checkStatus().getFileLists().toArray(new File[0]);

        int counter = 0;

        if (listOfServers.length == 0) {
            // handle no servers available
        } else {

            //Update servers first
            if (changedState = true) {
                updateServers();
            }

            for (int i = 0; i < listOfServers.length; i++) {
                fileList = listOfFileLists[i];

                try {
                    reader = new BufferedReader(new FileReader(fileList));
                    while ((line = reader.readLine()) != null) {
                        filesOnServer.add(line);
                    }

                    listing.add(filesOnServer);

                } catch (IOException e) {
                    System.out.println("[-] Error when reading filelists");
                    e.printStackTrace(); //TODO proper exception handling
                }
            }
        }

        if (counter == listOfServers.length) {
            return null; //All servers went down during list operation
        } else {
            return listing;
        }
    }

    public ServerList checkStatus() {
        ServerInterface allServers[] = {server1, server2, server3};
        File allFileLists[] = {fileList1, fileList2, fileList3};
        ArrayList<ServerInterface> previousUpServers = (ArrayList<ServerInterface>) upServers.clone(); //TODO check this
        ArrayList<File> upFileLists = new ArrayList<>();
        ServerList availableServers;

        for (int i = 0; i < 3; i++) {
            try {
                allServers[i].ping();
                upServers.add(allServers[i]);
                upFileLists.add(allFileLists[i]);
            } catch (RemoteException e) {
                System.out.println("[-] " + server1.toString() + "not available");
            }
        }

        if (!previousUpServers.equals(upServers)) {
            changedState = true;
        }

        availableServers = new ServerList(upServers, upFileLists);
        return availableServers;
    }
}

/* We either monitor using periodic UDP calls, if one fails we set a flag not to use it, keep sending out calls to it though when we get a succesful call.*/