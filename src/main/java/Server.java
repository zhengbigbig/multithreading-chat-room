import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket server;

    // TCP 端口 0~65535
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
    }

    public void start(){
        while (true){
            // 如果没有客户端连接就会一直阻塞在这里，若连接，则会返回一个Socket
            // 如果一个socket在读写，那么accept将不是阻塞状态，其他将不能连接
            Socket socket = server.accept();
        }
    }

    public static void main(String[] args) {

    }
}
