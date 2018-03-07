import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FrontEnd implements FrontEndInterface {

    private ServerInterface server1;
    private ServerInterface server2;
    private ServerInterface server3;

    private ArrayList<ServerInterface> upServers = new ArrayList<>();
    private boolean changedState = false; //Set to true when a server fails or comes back, to recheck files

    private File fileList1;
    private File fileList2;
    private File fileList3;


    private FrontEnd() {
    }

    public static void main(String args[]) {

        try {
            System.setProperty("java.security.policy", "server.policy");
            //Setup security manager
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            //Create front end, add it to registry to be used by clients
            FrontEnd frontEnd = new FrontEnd();
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 38048);

            //Create text documents holding the files that should exist on each server
            if (!(frontEnd.fileList1 = new File("server1.txt")).exists()) {
                frontEnd.fileList1.createNewFile();
            }
            if (!(frontEnd.fileList2 = new File("server2.txt")).exists()) {
                frontEnd.fileList2.createNewFile();
            }
            if (!(frontEnd.fileList3 = new File("server3.txt")).exists()) {
                frontEnd.fileList3.createNewFile();
            }

            FrontEndInterface stub = (FrontEndInterface) UnicastRemoteObject.exportObject(frontEnd, 0);
            registry.rebind("FrontEnd", stub);

            System.out.println("[+] Front End Server ready on port 38048");
        } catch (Exception e) {
            System.out.println("[-] Unable to run Front End Server: " + e.toString());
            e.printStackTrace();
        }
    }

    public boolean download(String filename) throws FileNotFoundException {
        ServerInterface server = null;
        ServerList availableServers;
        ArrayList<ServerInterface> listOfServers;
        ArrayList<File> listOfAvailableFileLists;
        ArrayList<ArrayList<String>> listOfListOfAvailableFiles;
        boolean exists = false;

        availableServers = checkStatus();
        listOfServers = availableServers.getServers();
        listOfAvailableFileLists = availableServers.getFileLists();
        listOfListOfAvailableFiles = getCorrectFiles(listOfAvailableFileLists);

        //Ensure there are servers available
        if (listOfServers.size() == 0) {
            return false;
        }

        //Update the necessary servers
        if (changedState) {
            updateServers(availableServers);
        }

        //Check to see if filename exists on any of the servers:
        for (int i = 0; i < listOfListOfAvailableFiles.size(); i++) {
            if (listOfListOfAvailableFiles.get(i).contains(filename)) {
                exists = true;
                server = listOfServers.get(i);
            }
        }

        if (exists) {
            try {
                if (server.download(9090)) {
                    TransferHelper getter = new TransferHelper(server.getIpAddress(), 9090, filename, "G");
                    Thread thread = new Thread(getter);
                    thread.run();
                } else {
                    return false;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException();
        }

        return true;
    }

    //Handles deleting a file from all servers
    public boolean delete(String filename) throws FileNotFoundException {
        ServerList availableServers;
        ArrayList<ServerInterface> listOfServers;
        ArrayList<File> listOfAllFileLists;
        ArrayList<ArrayList<String>> listOfListOfAllFiles;
        boolean exists = false;

        availableServers = checkStatus();
        listOfServers = availableServers.getServers();
        listOfAllFileLists = new ArrayList<>(Arrays.asList(fileList1, fileList2, fileList3));
        listOfListOfAllFiles = getCorrectFiles(listOfAllFileLists);

        //Ensure there are servers available
        if (listOfServers.size() == 0) {
            return false;
        }

        //Update the necessary servers
        if (changedState) {
            updateServers(availableServers);
        }

        //Check to see if filename exists on any of the servers:
        for (ArrayList<String> filesOnServer : listOfListOfAllFiles) {
            if (filesOnServer.contains(filename)) {
                exists = true;
            }
        }

        if (exists) {
            return remove(filename, listOfServers);
        } else {
            throw new FileNotFoundException();
        }
    }

    //Upload a file to front end server
    public boolean upload(int port, String filename, boolean reliable) {
        ServerList availableServers;
        ArrayList<ServerInterface> listOfServers;
        ArrayList<File> listOfAllFileLists;
        ArrayList<ArrayList<String>> listOfListOfAllFiles;
        boolean exists = false;

        availableServers = checkStatus();
        listOfServers = availableServers.getServers();
        listOfAllFileLists = new ArrayList<>(Arrays.asList(fileList1, fileList2, fileList3));
        listOfListOfAllFiles = getCorrectFiles(listOfAllFileLists);

        //Ensure there are servers available
        if (listOfServers.size() == 0) {
            return false;
        }

        //Update the necessary servers
        if (changedState) {
            updateServers(availableServers);
        }

        //Check to see if filename exists on any of the servers:
        for (ArrayList<String> filesOnServer : listOfListOfAllFiles) {
            if (filesOnServer.contains(filename)) {
                exists = true;
            }
        }

        //Start a thread to handle the upload
        TransferHelper helper;
        helper = new TransferHelper(port, "U", exists);
        Thread thread = new Thread(helper);
        thread.start();

        return true;
    }

    //Deletes the file off all the available servers it exists on, and if it exists
    // on a server that is not up, delete it from its file list, which will then be processed when
    //the server next comes online by the updateServers() method
    private boolean remove(String filename, ArrayList<ServerInterface> listOfAvailableServers) {
        ArrayList<ServerInterface> listOfAllServers;
        ArrayList<ArrayList<String>> listOfListOfAllFiles;
        ArrayList<File> listOfAllFileLists;

        listOfAllFileLists = new ArrayList<>(Arrays.asList(fileList1, fileList2, fileList3));
        listOfAllServers = new ArrayList<>(Arrays.asList(server1, server2, server3));
        listOfListOfAllFiles = getCorrectFiles(listOfAllFileLists);

        System.out.println("File exists");
        for (int i = 0; i < 3; i++) {
            if (listOfListOfAllFiles.get(i).contains(filename)) {
                System.out.println("Server: " + Integer.toString(i) + " contains file");
                if (listOfAvailableServers.contains(listOfAllServers.get(i))) {
                    System.out.println("This should execute");
                    try {
                        listOfAllServers.get(i).delete(filename);
                    } catch (RemoteException e) {
                        return false;
                    }
                }
                System.out.println(listOfListOfAllFiles);
                removeFileFromList(listOfAllFileLists.get(i), listOfListOfAllFiles.get(i), filename);
            }
        }
        return true;
    }

    //Handles pushing the files from the front end to one or many servers
    public void push(String filename, boolean exists, boolean reliable) {
        ServerList availableServers;

        ServerInterface emptiestServer = null;
        File emptiestServerList = null;
        ArrayList<String> emptiestServerFiles = null;

        ArrayList<ServerInterface> listOfAvailableServers;
        ArrayList<File> listOfAllAvailableFileLists;
        ArrayList<ArrayList<String>> listOfListOfAllAvailableFiles;

        int numFiles;
        int minimum = Integer.MAX_VALUE;

        //(Don't update servers as push() is only called if the upload() succeeds)

        //Get the AVAILABLE servers
        availableServers = checkStatus();
        listOfAvailableServers = availableServers.getServers();

        //If the file exists already (and the user wants to overwrite), remove the file
        if (exists) {
            if (!remove(filename, listOfAvailableServers)) {
                //TODO handle this - TURN PUSH INTO A BOOLEAN!!
            }
        }

        //Get the filelists, and the files within these filelists, of the AVAILABLE servers
        listOfAllAvailableFileLists = availableServers.getFileLists();
        listOfListOfAllAvailableFiles = getCorrectFiles(listOfAllAvailableFileLists);

        System.out.println();
        //TODO: handle no servers available - send back to client

        //Send the file to the server(s), depending on whether or not the client specified it as a reliable
        //upload. Each upload is done using a separate thread, each using its own socket, running in parallel

        if (reliable) { //TODO handle DUPLICATES file
            //Handle case where file is sent to all available servers
            for (int i = 0; i < listOfAvailableServers.size(); i++) {
                sendToServer(listOfAvailableServers.get(i), filename, 9091 + i);
                addFileToList(listOfAllAvailableFileLists.get(i), listOfListOfAllAvailableFiles.get(i), filename);
            }
        } else {
            //Handle single server case, by first finding the emptiest server
            for (int i = 0; i < listOfAvailableServers.size(); i++) {
                try {
                    numFiles = listOfAvailableServers.get(i).numFiles();
                    System.out.println("Server: " + Integer.toString(i));
                    System.out.println("No. of files on Server: " + Integer.toString(numFiles));

                    if (numFiles < minimum) {
                        minimum = numFiles;
                        emptiestServer = listOfAvailableServers.get(i);
                        emptiestServerList = listOfAllAvailableFileLists.get(i);
                        emptiestServerFiles = listOfListOfAllAvailableFiles.get(i);
                    }

                } catch (RemoteException e) {
                    //TODO
                }
            }

            sendToServer(emptiestServer, filename, 9091);
            addFileToList(emptiestServerList, emptiestServerFiles, filename);
        }
    }

    //Sends a single file from the front end to a single server
    private void sendToServer(ServerInterface server, String filename, int port) {
        try {
            //Calls server receive function, which starts a new thread and open a socket to connect to
            if (!server.receive(port)) {
                System.out.println("[-] No upload servers available, ");
            }

            //Create a new thread for the front end to push the file from the front end to the server
            TransferHelper uploader = new TransferHelper(server.getIpAddress(), port, filename, "P");
            Thread thread = new Thread(uploader);
            thread.run();

        } catch (RemoteException e) {
            //TODO handle
            e.printStackTrace();
        }
    }

    //Remove a file from a specific server filelist
    private void removeFileFromList(File filelist, ArrayList<String> files, String fileToRemove) {
        try {
            System.out.println("Contents of file:");
            BufferedReader reader = new BufferedReader(new FileReader(filelist));
            System.out.println(reader.readLine());

            files.remove(fileToRemove);
            System.out.print("What to write to file ");
            System.out.println(files);
            FileWriter writer = new FileWriter(filelist, false);

            writer.write("");

            for (String file : files) {
                writer.write(file + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace(); //TODO handle
        }
    }

    //Add a file to a specific server list
    private void addFileToList(File filelist, ArrayList<String> files, String fileToAdd) {
        try {
            files.add(fileToAdd);
            FileWriter writer = new FileWriter(filelist);

            for (String file : files) {
                writer.write(file + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace(); //TODO handle
        }
    }

    //Update all the available servers, so that the files on each server coincide with the files in their filelist
    private void updateServers(ServerList availableServers) {
        ServerInterface server;                                     //A single server
        ArrayList<ServerInterface> listOfServers;                   //An arraylist of available servers
        ArrayList<File> listOfFileLists;                            //An arraylist containing the fileList of each server
        ArrayList<String> correctFilesOnServer;                     //A list containing the files that should be on the server
        ArrayList<ArrayList<String>> listOfCorrectFilesOnServer;    //An arraylist containing the list of files that should be on the server, for each server
        ArrayList<String> actualFilesOnServer;                      //A list containing the files actually on the server
        ArrayList<ArrayList<String>> listOfActualFilesOnServer;     //An arraylist containing the list of files actually on the server, for each server

        listOfServers = availableServers.getServers();
        listOfFileLists = availableServers.getFileLists();

        listOfActualFilesOnServer = getActualFiles(listOfServers);
        listOfCorrectFilesOnServer = getCorrectFiles(listOfFileLists);

        for (int i = 0; i < listOfActualFilesOnServer.size(); i++) {
            actualFilesOnServer = listOfActualFilesOnServer.get(i);
            correctFilesOnServer = listOfCorrectFilesOnServer.get(i);
            server = listOfServers.get(i);

            for (String file : actualFilesOnServer) {
                if (!correctFilesOnServer.contains(file)) {
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

    //Return a list of unique files on the server
    public Set<String> list() { //TODO: add duplicates?
        ServerList availableServers = checkStatus();
        ArrayList<ArrayList<String>> listOfLists;
        Set<String> listing = new HashSet<>();
        ArrayList<ServerInterface> servers = availableServers.getServers();
        ArrayList<File> filelists = availableServers.getFileLists();
        if (servers.size() == 0) {
            return null;
        }

        //Update servers first if necessary
        if (changedState) {
            updateServers(availableServers);
        }

        listOfLists = getCorrectFiles(filelists);

        for (ArrayList<String> list : listOfLists) {
            for (String file : list) {
                listing.add(file);
            }
        }
        return listing;
    }

    //Get a list containing lists of all the files currently held on each server
    private ArrayList<ArrayList<String>> getActualFiles(ArrayList<ServerInterface> servers) {

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

    //Get a list containg lists of all the files that should be held on each server
    private ArrayList<ArrayList<String>> getCorrectFiles(ArrayList<File> listOfFileLists) { //TODO: add duplicates???
        System.out.println(listOfFileLists);
        File fileList;
        String line;
        BufferedReader reader;

        ArrayList<String> filesOnServer = new ArrayList<>();
        ArrayList<ArrayList<String>> listing = new ArrayList<>();

        int counter = 0;

        if (listOfFileLists.size() == 0) {
            // TODO: handle no servers available, make sure calling functions properly handle
        } else {

            for (int i = 0; i < listOfFileLists.size(); i++) {
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

                filesOnServer = new ArrayList<>();
            }
        }

        if (counter == listOfFileLists.size()) {
            return null; //All servers went down during list operation
        } else {
            return listing;
        }
    }

    //Check to see which servers are online, and if there has been a change of state of any of the servers
    private ServerList checkStatus() {

        ArrayList<ServerInterface> stubs = new ArrayList<>();
        ArrayList<File> fileLists = new ArrayList<>();
        ArrayList<ServerInterface> previousUpServers = (ArrayList<ServerInterface>) upServers.clone(); //TODO check this
        ArrayList<File> upFileLists = new ArrayList<>();
        ServerList availableServers;
        Registry registry;

        //Get registry
        try {
            registry = LocateRegistry.getRegistry("127.0.0.1", 38048);
        } catch (RemoteException e) {
            System.out.println("[-] Front end unable to access registry");
            return null; //TODO handle
        }

        //Get updated stubs, in case a server has come back online and rebound their stub to the registry
        try {
            server1 = (ServerInterface) registry.lookup("Server1");
            stubs.add(server1);
            fileLists.add(fileList1);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace(); //TODO fix these
        }

        try {
            server2 = (ServerInterface) registry.lookup("Server2");
            stubs.add(server2);
            fileLists.add(fileList2);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        try {
            server3 = (ServerInterface) registry.lookup("Server3");
            stubs.add(server3);
            fileLists.add(fileList3);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        if (stubs.size() == 0) {
            System.out.println("[-] No server stubs registered in registry");
            return null; //TODO handle
        }

        //Check servers haven't gone down by pinging them (stubs might be registered but server may be down
        upServers = new ArrayList<>();
        for (int i = 0; i < stubs.size(); i++) {
            try {
                stubs.get(i).ping();
                upServers.add(stubs.get(i));
                upFileLists.add(fileLists.get(i));

            } catch (RemoteException e) {
                System.out.println("[-] Server" + Integer.toString(i + 1) + " not available");
            }
        }

        if (!previousUpServers.equals(upServers)) {
            changedState = true;
        }

        availableServers = new ServerList(upServers, upFileLists);
        return availableServers;
    }
}

/* We either monitor using periodic UDP calls, if one fails we set a flag not to use it, keep sending out calls to it though when we get a successful call.*/