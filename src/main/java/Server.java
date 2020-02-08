import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Server {
    private static AtomicInteger COUNTER = new AtomicInteger(0);
    private final ServerSocket server;
    private final Map<Integer, ClientConnection> clients = new ConcurrentHashMap<>();

    // TCP 端口 0~65535
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
    }

    public void start() throws IOException {
        while (true) {
            // 如果没有客户端连接就会一直阻塞在这里，若连接，则会返回一个Socket
            // 如果一个socket在读写，那么accept将不是阻塞状态，其他将不能连接
            // 因此需要多线程
            Socket socket = server.accept();
            new ClientConnection(COUNTER.incrementAndGet(), this, socket).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server(8080).start();
    }

    public String getAllClientsInfo() {
        return clients.entrySet().stream().map(client -> client.getKey() + ":" + client.getValue().getClientName()).collect(Collectors.joining(","));
    }

    // 注册后上线
    public void registerClient(ClientConnection clientConnection) {
        clients.put(clientConnection.getClientId(), clientConnection);
        this.clientOnline(clientConnection);
    }

    // 上线发送消息
    public void clientOnline(ClientConnection clientWhoHasJustLoggedIn) {
        clients.values().forEach(client -> {
            dispatchMessage(client, "系统", "所有人", clientWhoHasJustLoggedIn.getClientName() + "上线了" + getAllClientsInfo());

        });
    }

    public void sendMessage(ClientConnection src, Message message) {
        if (message.getId() == 0) {
            clients.values().forEach(client -> {
                dispatchMessage(client, src.getClientName(), "所有人", message.getMessage());
            });
        } else {
            int targetUser = message.getId();
            ClientConnection target = clients.get(targetUser);
            if (target == null) {
                System.out.println("用户" + targetUser + "不存在");
            } else {
                dispatchMessage(target, src.getClientName(), "你", message.getMessage());
            }
        }
    }

    public void clientOffline(ClientConnection clientConnection) {
        clients.remove(clientConnection.getClientId());
        clients.values().forEach(client -> {
            dispatchMessage(client, "系统", clientConnection.getClientName(), "下线了" + getAllClientsInfo());
        });
    }

    private void dispatchMessage(ClientConnection client, String src, String target, String message) {
        try {
            client.sendMessage(src + "对" + target + "说：" + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ClientConnection extends Thread {
    private Socket socket;
    private Integer clientId;
    private String clientName;
    private Server server;

    ClientConnection(int clientId, Server server, Socket socket) {
        this.clientId = clientId;
        this.server = server;
        this.socket = socket;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.defaultCharset()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (isNotOnlineYet()) {
                    clientName = line;
                    server.registerClient(this);
                } else {
                    Message message = JSON.parseObject(line, Message.class);
                    server.sendMessage(this, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.clientOffline(this);
        }
    }

    private boolean isNotOnlineYet() {
        return clientName == null;
    }

    public void sendMessage(String message) throws IOException {
        Util.writeMessage(socket, message);
    }
}

class Message {
    private Integer id;
    private String message;

    // json反序列化需要这个构造器创建
    Message() {
    }

    Message(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}