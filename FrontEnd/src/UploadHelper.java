import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class UploadHelper implements Runnable {
    int port;
    boolean type;
    boolean reliable;
    ServerSocket listener;
    Socket socket;

    //Used for uploading files
    UploadHelper(int port, boolean type, boolean reliable) {
        this.port = port;
        this.type = type; //True for upload, false for download
        this.reliable = reliable;
    }

    //Used for downloading files
    UploadHelper(int port, boolean type) {
        this.port = port;
        this.type = type; //True for upload, false for download
    }

    public void run() {
        try {
            listener = new ServerSocket(port);

            while (true) {
                socket = listener.accept();
                break;
            }

            if (type) {
                upload();
            } else {
                download();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void upload() {
        System.out.println("[+] Upload successful");
    }

    public void download() {

    }
}
