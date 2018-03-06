import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TransferHelper implements Runnable {
    int port;
    String type;
    boolean exists;
    ServerSocket listener;
    Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private BufferedInputStream bis;

    //Client -> FrontEnd
    TransferHelper(int port, String type, boolean exists) {
        this.port = port;
        this.type = type; //True for upload, false for download
        this.exists = exists;
    }

    //FrontEnd -> Client and FrontEnd -> Server
    TransferHelper(int port, String type) {
        this.port = port;
        this.type = type; //True for upload, false for download
    }

    public void run() {

        System.setProperty("java.security.policy", "server.policy"); //TODO test if required

        try {
            listener = new ServerSocket(port);

            while (true) {
                socket = listener.accept();
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                bis = new BufferedInputStream(socket.getInputStream());
                break;
            }

            if (type.equals("U")) {
                upload();
            } else if (type.equals("D")) {
                download();
            } else if (type.equals("P")) {
                push();
            } else {
                //Incorrect type given
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void upload() {
        int filenameLength;
        int filesize;
        int readBytes;
        int totalBytes = 0;
        byte[] buffer = new byte[1024];
        StringBuilder filename = new StringBuilder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileOutputStream fileOutputStream;
        File outputFile;

        try {
            //Read filename length
            filenameLength = dis.readInt();

            //Read filename
            for (int i = 0; i < filenameLength; i++) {
                filename.append(dis.readChar());
            }

            //Check if file exists, and if user wants to overwrite
            if (exists) {
                dos.writeInt(-1);

                if (dis.readInt() != 0) {
                    System.out.println("[-] User cancelled upload");
                    try {
                        socket.close();
                        listener.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return;
                }

            } else {
                dos.writeInt(0);
            }

            //Send ready to receive bytes
            dos.writeInt(0);
        } catch (IOException e) {
            System.out.println("[-] Unable to read filename or filesize");

            try {
                socket.close();
                listener.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }

        try {
            //Read file size
            filesize = dis.readInt();

            //Receive and read bytes
            while (totalBytes < filesize) {
                readBytes = bis.read(buffer);
                outputStream.write(buffer, 0, readBytes);
                totalBytes += readBytes;
            }

            //Send number of bytes received
            buffer = outputStream.toByteArray();
            dos.writeInt(buffer.length);

            //Write buffer to file
            if (buffer.length == filesize) {
                try {
                    outputFile = new File("files/" + filename.toString());
                    outputFile.createNewFile();
                    fileOutputStream = new FileOutputStream(outputFile);
                    fileOutputStream.write(buffer);
                } catch (IOException e) {
                    System.out.println("[-] Unable to save file");
                }
            } else {
                System.out.println("[-] Incorrect number of bytes received, file not saved");
            }
        } catch (IOException e) {
            System.out.println("[-] Unable to read file ");
        }

        try {
            socket.close();
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void download() {

    }

    public void push() {
        int filenameLength;
        int filesize;
        int readBytes;
        int totalBytes = 0;
        byte[] buffer = new byte[1024];
        StringBuilder filename = new StringBuilder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileOutputStream fileOutputStream;
        File outputFile;

        try {
            //Read filename length
            filenameLength = dis.readInt();

            //Read filename
            for (int i = 0; i < filenameLength; i++) {
                filename.append(dis.readChar());
            }

            //Read filesize
            filesize = dis.readInt();

            //Receive and read bytes
            while (totalBytes < filesize) {
                readBytes = bis.read(buffer);
                outputStream.write(buffer, 0, readBytes);
                totalBytes += readBytes;
            }

            //Write buffer to file
            if (buffer.length == filesize) {
                try {
                    outputFile = new File("files/" + filename.toString());
                    outputFile.createNewFile();
                    fileOutputStream = new FileOutputStream(outputFile);
                    fileOutputStream.write(buffer);
                } catch (IOException e) {
                    //TODO handle
                }
            } else {
                //TODO handle
            }

        } catch (IOException e) {
            e.printStackTrace();
        }





    }
}
