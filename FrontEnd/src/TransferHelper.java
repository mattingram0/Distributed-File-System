import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

class TransferHelper implements Runnable {
    private int port;
    private String host;
    private String filename;
    private String type;
    private boolean exists;
    private ServerSocket listener;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private BufferedInputStream bis;

    //FrontEnd -> Server, Server -> FrontEnd
    TransferHelper(String host, int port, String filename, String type) {
        this.host = host;
        this.port = port;
        this.filename = filename;
        this.type = type;
    }

    //Client -> FrontEnd
    TransferHelper(int port, String type, boolean exists) {
        this.port = port;
        this.type = type; //True for upload, false for download
        this.exists = exists;
    }

    //Server -> FrontEnd
    TransferHelper(int port, String type, String filename) {
        this.port = port;
        this.type = type;
        this.filename = filename;
    }

    //FrontEnd -> Client and FrontEnd -> Server
    TransferHelper(int port, String type) {
        this.port = port;
        this.type = type; //True for upload, false for download
    }

    public void run() {

        System.setProperty("java.security.policy", "server.policy"); //TODO test if required

        if (type.equals("P")) {
            try {
                while (true) {
                    try {
                        socket = new Socket(host, port);
                    } catch (ConnectException c) {
                        continue;
                    }
                    break;
                }

                push();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (type.equals("G")) {
            try {
                while (true) {
                    try {
                        socket = new Socket(host, port);
                    } catch (ConnectException c) {
                        continue;
                    }
                    break;
                }

                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                bis = new BufferedInputStream(socket.getInputStream());

                receive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                listener = new ServerSocket(port);
                System.out.println("listener created on: " + Integer.toString(port));

                socket = listener.accept();
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                bis = new BufferedInputStream(socket.getInputStream());

                if (type.equals("U")) {
                    upload();
                } else if (type.equals("D")) {
                    System.out.println("dwld command received");
                    push();
                } else if (type.equals("R")) {
                    receive();
                } else {
                    //Incorrect type given
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void upload() { //TODO change upload and push just to use filename given by client????????
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
            if (totalBytes == filesize) {
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
            e.printStackTrace();
            System.out.println("[-] Unable to read file ");
        }

        try {
            socket.close();
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void push() {
        int readBytes;
        byte[] buffer;
        File uploadFile;
        ByteArrayOutputStream byteOutputStream;
        BufferedInputStream bfis;
        DataOutputStream dos;
        BufferedOutputStream bos;

        try {
            //Create the socket connection to handle the file transfer (only)

            //Create output streams
            dos = new DataOutputStream(socket.getOutputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());

            //Check file exists
            if (new File("files/" + filename).isFile()) {
                System.out.println(filename);
                System.out.println("qabgriqbfribqf");
                System.out.println(filename);
                uploadFile = new File("files/" + filename);
                bfis = new BufferedInputStream(new FileInputStream(uploadFile));
                byteOutputStream = new ByteArrayOutputStream();

                buffer = new byte[1024];

                //Read file into buffer
                while ((readBytes = bfis.read(buffer)) > 0) {
                    byteOutputStream.write(buffer, 0, readBytes);
                }
            } else {
                //TODO handle this case that somehow the file uploaded to the front end can't be found
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("file not found!!!!");
                return;
            }

            //Send filename length and filename
            dos.writeInt(filename.length());
            dos.writeChars(filename);
            dos.flush();

            //Send file size
            buffer = byteOutputStream.toByteArray();
            dos.writeInt(buffer.length);

            //Send file
            bos.write(buffer, 0, buffer.length);
            bos.flush();

            socket.close();

            if (listener != null) {
                listener.close();
            }

            //As multiple threads may be using the same file, all will run until last one using it deletes file
//            while (uploadFile.exists()) {
//                uploadFile.delete();
//            }

        } catch (IOException e) {
            System.out.println("push error" + e.getMessage());
            //TODO
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receive() {

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

            buffer = outputStream.toByteArray();

            //Write buffer to file
            if (totalBytes == filesize) {
                try {
                    outputFile = new File("files/" + filename.toString());
                    outputFile.createNewFile();
                    fileOutputStream = new FileOutputStream(outputFile);
                    fileOutputStream.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    //TODO handle
                }

            } else {
                //TODO handle
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();

            if (listener != null) {
                listener.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
