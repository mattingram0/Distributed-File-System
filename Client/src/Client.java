import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

class Client {

    private Socket socket;
    private boolean connected = false;
    private DataInputStream dis;
    private DataOutputStream dos;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private String host;
    private int port;
    private FrontEndInterface frontEnd;
    private Registry registry;

    private Client() {
    }

    public static void main(String args[]) {

        //Prompt for operation - must be connect

        Client client;
        String command;
        Scanner scanner;

        scanner = new Scanner(System.in);
        client = new Client();
        client.printCommands();

        dance:
        while (true) {

            command = client.getCommand(scanner);

            if (client.connected) {
                if (!client.checkConnected()) {
                    client.closeConnection();
                    if (!command.equals("CONN")) {
                        System.out.println("[-] Connection closed by " + client.socket.getInetAddress().getHostName() + ", please reconnect to a server\n");
                        continue;
                    } else {
                        System.out.println("[*] Previous connection to " + client.socket.getInetAddress().getHostName() + " unexpectedly dropped, continuing with new connection");
                    }
                }
            }

            switch (command) { //TODO Add time out handling, ADD FINAL TO ALL TRY CATCH TO FLUSH STREAMS ETC
                case "CONN":

                    if (client.connected) {
                        System.out.print("[*] Already connected to " + client.socket.getInetAddress().getHostName() + " on port " + client.socket.getPort() + ". ");
                        System.out.println("Are you sure you want to connect to a new host? (y/n)");
                        System.out.print("> ");

                        if (!client.yesNo(scanner)) {
                            System.out.println();
                            break;
                        } else {
                            client.closeConnection();
                        }
                    }

                    try {
                        client.connect(scanner);
                    } catch (InputException e) {
                        System.out.println("[-] " + e.getMessage());
                        System.out.println();
                    }

                    break;

                case "UPLD": //TODO Add proper client connection checks here
                    if (client.connected) {
                        try {
                            client.upload(scanner);
                        } catch (TransferException e) {
                            System.out.println("[-] " + e.getMessage());
                            System.out.println();
                        }
                    } else {
                        System.out.println("[-] Not connected to a server! Please use CONN to connect to a fileserver \n");
                    }
                    break;


                case "LIST":
                    if (client.connected) {
                        client.list();
                    } else {
                        System.out.println("[-] Not connected to a server! Please use CONN to connect to a fileserver \n");
                    }

                    break;

                case "DWLD":
                    if (client.connected) {
                        client.download(scanner);
                    } else {
                        System.out.println("[-] Not connected to a server! Please use CONN to connect to a fileserver \n");
                    }
                    break;
                case "DELF":
                    if (client.connected) {
                        client.delete(scanner);
                    } else {
                        System.out.println("[-] Not connected to a server! Please use CONN to connect to a fileserver \n");
                    }
                    break;
                case "QUIT":
                    if (client.quit(scanner)) {
                        client.closeConnection();
                        System.exit(0);
                    } else {
                        break;
                    }

                case "HELP":
                    break;
                default:
                    System.out.println("[-] Unknown command, please try again\n");
            }
        }
    }

    private boolean checkConnected() {
        return true;
        //TODO
    }

    private boolean yesNo(Scanner scanner) {
        String input = scanner.nextLine().toLowerCase();

        while (!input.equals("y") && !input.equals("n")) {
            input = scanner.nextLine().toLowerCase();
            System.out.println("[-] Invalid input, please enter y for yes, or n for no");
            System.out.print("> ");
        }

        return input.equals("y");
    }

    private void closeConnection() {

    }

    private void download(Scanner scanner) {
        String filename;
        String progressBar;
        int filesize;
        int bytesRemaining;
        int readBytes;
        double percentage;
        int totalBytes = 0;
        byte[] buffer = new byte[1024];
        FileOutputStream fileOutputStream;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        float startTime;
        float endTime;
        float downloadTime;

        try {
            //Get filename
            System.out.println("[*] Please enter filename to download");
            System.out.print("> ");
            filename = scanner.nextLine();

            if (new File(filename).isFile()) {
                System.out.println("[*] File already exists locally, overwrite? (y/n)");
                System.out.print("> ");

                //Check for overwrites
                if (!yesNo(scanner)) {
                    System.out.println("[-] Download abandoned by the user\n");
                    return;
                }
            }

            //Call the remote download method
            try {
                if (!frontEnd.download(9090, filename)) {
                    System.out.println("[-] Unable to download file from server");
                    return;
                }
            } catch (FileNotFoundException e) {
                System.out.println("[-] File does not exist on server");
                return;
            }

            try {
                //Create the socket connection to handle the file transfer (only)
                while (true) {
                    try {
                        socket = new Socket(host, 9090);
                    } catch (ConnectException c) {
                        continue;
                    }
                    break;
                }

                //Create the input and output streams for file transfer
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                bis = new BufferedInputStream(socket.getInputStream());
                bos = new BufferedOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                //TODO proper error handling
            }

            //Send filename length and filename, receive filesize
            dos.writeInt(filename.length());
            dos.writeChars(filename);
            dos.flush();

            filesize = dis.readInt();
            bytesRemaining = filesize;

            //If the file exists:
            if (filesize != -1) {

                startTime = System.nanoTime();
                //Read the bytes from the socket, displaying a progress bar
                while (bytesRemaining > 0) {
                    percentage = 100 * ((float) totalBytes / (float) filesize);

                    if ((int) percentage == 0.0) {
                        progressBar = "[                                                                                                    ]" + "0%\r";
                    } else {
                        progressBar = "[" + String.format("%0" + (int) percentage + "d" + "%" + (100 - (int) percentage) + "s", 0, "").replace("0", "=") + "]" + Integer.toString((int) percentage) + "%\r";
                    }

                    System.out.print(progressBar);
                    readBytes = bis.read(buffer, 0, Math.min(buffer.length, bytesRemaining));

                    if (readBytes < 0) {
                        break; //We expected more bytes but some were lost, will get handled after
                    }

                    bytesRemaining -= readBytes;
                    totalBytes += readBytes;
                    outputStream.write(buffer, 0, readBytes);
                }

                progressBar = "[==================================================================================================]" + "100%\r";
                System.out.println(progressBar);

                buffer = outputStream.toByteArray();
                endTime = System.nanoTime();
                downloadTime = (endTime - startTime) / 1000000;

                //If the correct number of bytes were received, write the file
                if (totalBytes == filesize) {
                    fileOutputStream = new FileOutputStream(filename);
                    fileOutputStream.write(buffer);
                    System.out.println("[+] " + filename + " successfully downloaded - " + Integer.toString(totalBytes) + " bytes received in " + Float.toString(downloadTime) + "ms\n");
                } else { //Incorrect number of bytes received
                    System.out.println("[-] " + filename + " not successfully downloaded: " + Integer.toString(buffer.length) + "/" + Integer.toString(totalBytes) + " bytes received in" + Float.toString(downloadTime) + "ms\n");
                }

                //File does not exist
            } else {
                System.out.println("[-] The file does not exist on server\n");
            }
        } catch (IOException e) {
            //TODO add proper exception handling
            e.printStackTrace();
        }
    }

    private void delete(Scanner scanner) {
        String filename;

        System.out.println("[*] Please enter filename to delete");
        System.out.print("> ");
        filename = scanner.nextLine();

        try {
            if (frontEnd.delete(filename)) {
                System.out.println("[+] File deleted successfully");
            } else {
                throw new RemoteException();
            }
        } catch (RemoteException e) {
            System.out.println("[-] Server error whilst deleting file, file not deleted");
        } catch (FileNotFoundException f) {
            System.out.println("[-] File does not exist on the server");
        }
    }

    private void upload(Scanner scanner) throws TransferException {
        String filename;
        String progressBar;
        ByteArrayOutputStream byteOutputStream;
        BufferedInputStream bfis;
        File uploadFile;
        byte[] buffer;
        int readBytes;
        int bytesReceived;
        int status;
        float startTime;
        float endTime;
        float uploadTime;
        boolean reliable;
        boolean exists = false;

        //Get filename
        try {
            System.out.println("[*] Please enter filename to upload");
            System.out.print("> ");
            filename = scanner.nextLine();

            //Ensure file exists
            while ((!new File(filename).isFile())) {
                System.out.println("[-] File not found, please try again");
                System.out.print("> ");
                filename = scanner.nextLine();
            }

            uploadFile = new File(filename);
            bfis = new BufferedInputStream(new FileInputStream(uploadFile));
            byteOutputStream = new ByteArrayOutputStream();
        } catch (IOException e) {
            System.out.println("[-] File not found");
            return;
        }

        //Check if file needs to be stored reliably
        System.out.println("[*] Backup file across multiple servers? (y/n)");
        System.out.print("> ");
        reliable = yesNo(scanner);

        //Calls the remote upload function, which tells the front end to open a remote connection
        //This step ensures the front ends listener socket is open before we try connect to it
        try {
            if (!frontEnd.upload(9090, filename, reliable)) {
                System.out.println("[-] No upload servers available, ");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            //Create the socket connection to handle the file transfer (only)
            while (true) {
                try {
                    socket = new Socket(host, 9090);
                } catch (ConnectException c) {
                    continue;
                }
                break;
            }

            //Create the input and output streams for file transfer
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            bis = new BufferedInputStream(socket.getInputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            //TODO proper error handling
        }

        try {
            buffer = new byte[1024];

            //Read file into bytes buffer
            while ((readBytes = bfis.read(buffer)) > 0) {
                byteOutputStream.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            System.out.println("[-] Unable to read file ");
            return;
        }

        try {
            buffer = byteOutputStream.toByteArray();

            //Send filename length and filename
            dos.writeInt(filename.length());
            dos.writeChars(filename);
            dos.flush();

            //Check if file exists or not before upload:
            if (dis.readInt() == -1) {
                exists = true;
                System.out.println("[*] File already exists on remote server, overwrite? (y/n)");
                System.out.print("> ");

                if (yesNo(scanner)) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(-1);
                    System.out.println("");
                    return;
                }

            } else {
                exists = false;
            }

            //read status
            status = dis.readInt(); //TODO BISBOS
            if (status == -1) {
                throw new IOException();
            }

        } catch (IOException e) {
            System.out.println("[-] No okay received from server");
        }

        try {
            //Send file length
            dos.writeInt(buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Send file over stream, in chunks 100 of the total length
        int chunkLength = buffer.length / 100;
        int difference = buffer.length - (chunkLength * 100);
        startTime = System.nanoTime();

        try {
            if (buffer.length > 100) { //If file over 100 bytes, use progress bar
                for (int i = 0; i < 100; i++) {
                    bos.write(buffer, i * chunkLength, chunkLength);

                    if (i == 0) {
                        progressBar = "[                                                                                                    ]" + "0%\r";
                    } else {
                        progressBar = "[" + String.format("%0" + i + "d" + "%" + (100 - i) + "s", 0, "").replace("0", "=") + "]" + Integer.toString(i) + "%\r";
                    }

                    System.out.print(progressBar);
                }

                bos.write(buffer, 100 * chunkLength, difference);
                bos.flush(); //CRITICAL TO SEND THE LAST 21 bytes
                progressBar = "[==================================================================================================]" + "100%\r";
                System.out.println(progressBar);

            } else {
                //File too small to use the progress bar
                bos.write(buffer);
                bos.flush();
            }

            ///Users/matt/Documents/Durham/2nd Year/Networks and Systems/Assignment/client/test.jpg
        } catch (IOException e) {
            throw new TransferException("Error whilst sending file");
        }

        try {
            bytesReceived = dis.readInt(); //TODO BISBOS
            endTime = System.nanoTime();
            uploadTime = (endTime - startTime) / 1000000;

            if (bytesReceived == buffer.length) {
                System.out.println("[+] " + filename + " successfully uploaded: " + Integer.toString(buffer.length) + " bytes received in " + Float.toString(uploadTime) + "ms");

                //If the upload to the front end was successful, push the uploads to the other servers.
                frontEnd.push(filename, exists, reliable);
            } else {
                System.out.println("[-] " + filename + " not successfully uploaded: " + Integer.toString(bytesReceived) + "/" + Integer.toString(buffer.length) + " bytes received in" + Float.toString(uploadTime) + "ms");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("[-] Unable to read number of bytes received");
        }

        System.out.println("");
    }

    private void list() {
        Set<String> listing;

        try {
            listing = frontEnd.list();

            if (listing == null) {
                System.out.println("[-] Server error, unable to retrieve directory listing");
            } else {
                for (String file : listing) {
                    System.out.println(file); //TODO: TEST
                }
            }
        } catch (RemoteException e) {
            System.out.println("[-] Server error: " + e.getMessage());
        }
    }

    private void connect(Scanner scanner) throws InputException {

        System.out.println("[*] Please enter a hostname or IP address to connect to: ");
        System.out.print("> ");

        try {
            host = scanner.nextLine();
        } catch (NoSuchElementException | IllegalStateException e) {
            throw new InputException("Error when reading hostname");
        }

        System.out.println("[*] Please enter the port: ");
        System.out.print("> ");

        try {
            port = scanner.nextInt();
            scanner.nextLine();
        } catch (IllegalStateException | InputMismatchException e) {
            throw new InputException("Unable to read port number");
        }

        try {
            //Connect to the registry
            registry = LocateRegistry.getRegistry(host, port);
            frontEnd = (FrontEndInterface) registry.lookup("FrontEnd");

            System.out.println("[+] Successfully connected to " + host + " on port " + Integer.toString(port) + "\n");
            connected = true;

        } catch (Exception e) {
            throw new InputException("Unable to connect to front end server: " + e.toString());
        }
    }

    private boolean quit(Scanner scanner) {
        return true;
    }

    private void printCommands() {
        System.out.println("[*] Please input an operation:\n" + "CONN - Connect to a Server\n" +
                "UPLD - Upload a File\n" + "LIST - List files on the Server\n" + "DWLD - Download a File\n" +
                "DELF - Delete a File\n" + "QUIT - Exit the Server\n" + "HELP - Detailed help on the commands\n" +
                "'>>>' denotes the client is ready for a command, '>' denotes the client is waiting for user input\n");
    }

    private String getCommand(Scanner scanner) {
        System.out.print(">>> ");
        return scanner.nextLine();
    }

    protected void finalize() {

    }
}
