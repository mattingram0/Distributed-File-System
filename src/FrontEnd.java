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
        ServerInterface emptiestServer = null;
        ServerList availableServers = checkStatus();
        ArrayList<ServerInterface> listOfServers = availableServers.getServers();
        int numFiles;
        int minimum = Integer.MAX_VALUE;

        //Ensure there are servers available
        if (listOfServers.size() == 0) {
            // TODO handle no servers available
        }

        //Update the necessary servers
        if (changedState = true) {
            updateServers(availableServers);
        }

        //Find the emptiest server
        for (ServerInterface server : listOfServers) {
            try {
                numFiles = server.numFiles();

                if (numFiles < minimum) {
                    minimum = numFiles;
                    emptiestServer = server;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (emptiestServer == null) {
            // handle all servers failed to respond to list operation
        } else {
            //create socket connection to handle the upload, check the upload doesn't exist on any of them
        }
    }

    public void updateServers(ServerList availableServers) {
        ServerInterface server; //A single server
        ArrayList<ServerInterface> listOfServers = new ArrayList<>(); //An arraylist of available servers
        ArrayList<File> listOfFileLists; //An arraylist containing the fileList of each server
        ArrayList<String> correctFilesOnServer; //A list containing the files that should be on the server
        ArrayList<ArrayList<String>> listOfCorrectFilesOnServer; //An arraylist containing the list of files that should be on the server, for each server
        ArrayList<String> actualFilesOnServer; //A list containing the files actually on the server
        ArrayList<ArrayList<String>> listOfActualFilesOnServer; //An arraylist containing the list of files actually on the server, for each server
        ArrayList<String> duplicateFiles; //An arraylist containing all files that should be on every server

        listOfServers = availableServers.getServers();
        listOfFileLists = availableServers.getFileLists();

        listOfActualFilesOnServer = getActualFiles(listOfServers);
        listOfCorrectFilesOnServer = getCorrectFiles(listOfFileLists);
        duplicateFiles = getDuplicateFiles();

        for (int i = 0; i < listOfActualFilesOnServer.size(); i++) {
            actualFilesOnServer = listOfActualFilesOnServer.get(i);
            correctFilesOnServer = listOfCorrectFilesOnServer.get(i);
            server = listOfServers.get(i);

            for (String file : actualFilesOnServer) {
                if (!correctFilesOnServer.contains(file) && !duplicateFiles.contains(file)) {
                    try {
                        server.delete(file);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        changedState = false;
    }

    public ArrayList<ArrayList<String>> list() { //TODO: add duplicates?
        ServerList availableServers = checkStatus();
        ArrayList<ArrayList<String>> listing;
        ArrayList<ServerInterface> servers = availableServers.getServers();

        if (servers.size() == 0) {
            return null;
        }

        //Update servers first if necessary
        if (changedState = true) {
            updateServers(availableServers);
        }

        listing = getActualFiles(servers);
        listing.add(getDuplicateFiles());
        return listing;

    }

    private ArrayList<String> getDuplicateFiles() {
        ArrayList<String> duplicateFiles = new ArrayList<>();
        String line;
        BufferedReader reader;

        //Read in the duplicate files
        try {
            reader = new BufferedReader(new FileReader(duplicates));

            while ((line = reader.readLine()) != null) {
                duplicateFiles.add(line);
            }
        } catch (IOException e) {
            //TODO
        }

        return duplicateFiles;
    }

    public ArrayList<ArrayList<String>> getActualFiles(ArrayList<ServerInterface> servers) {

        ArrayList<ArrayList<String>> listing = new ArrayList<>();
        int counter = 0;

        if (servers.size() == 0) {
            return null; //No servers available TODO: handle this on client side
        } else {
            for (ServerInterface server : servers) {
                try {
                    listing.add(server.list());
                } catch (RemoteException e) {
                    System.out.println("[-] " + server.toString() + "failed during list operation");
                    counter++;
                }
            }
        }

        if (counter == servers.size()) {
            return null; //All servers went down during list operation
        } else {
            return listing;
        }
    }

    public ArrayList<ArrayList<String>> getCorrectFiles(ArrayList<File> listOfFileLists) { //TODO: add duplicates???
        File fileList;
        String line;
        BufferedReader reader;

        ArrayList<String> filesOnServer = new ArrayList<>();
        ArrayList<ArrayList<String>> listing = new ArrayList<>();

        int counter = 0;

        if (listOfFileLists.size() == 0) {
            // TODO: handle no servers available, make sure calling functions properly handle
        } else {

            for (int i = 0; i < listOfFileLists.size(); i++) { //TODO: abstract this out into a new function, or make update servers return this??
                fileList = listOfFileLists.get(i);

                try {
                    reader = new BufferedReader(new FileReader(fileList));
                    while ((line = reader.readLine()) != null) {
                        filesOnServer.add(line);
                    }

                    listing.add(filesOnServer);

                } catch (IOException e) {
                    System.out.println("[-] Error when reading filelists");
                    counter++;
                    e.printStackTrace(); //TODO proper exception handling
                }
            }
        }

        if (counter == listOfFileLists.size()) {
            return null; //All servers went down during list operation
        } else {
            return listing;
        }
    }

    public ServerList checkStatus() {
        ServerInterface allServers[] = {server1, server2, server3}; //All the logic in the program relies on the indexes being the same for these two arrays
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