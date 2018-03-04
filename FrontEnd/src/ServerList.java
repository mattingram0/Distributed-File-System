import java.io.File;
import java.util.ArrayList;

public class ServerList {
    private ArrayList<ServerInterface> servers;
    private ArrayList<File> fileLists;

    public ServerList(ArrayList<ServerInterface> servers, ArrayList<File> fileLists) {
        this.servers = servers;
        this.fileLists = fileLists;
    }

    public ArrayList<File> getFileLists() {
        return fileLists;
    }

    public ArrayList<ServerInterface> getServers() {
        return servers;
    }
}
