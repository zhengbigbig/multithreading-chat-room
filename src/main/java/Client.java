import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("输入您的昵称");
        Scanner userInput = new Scanner(System.in);
        String name = userInput.nextLine();

        Socket socket = new Socket("127.0.0.1", 8080);

        Util.writeMessage(socket, name);

        System.out.println("连接成功！");

        // 从服务端读取socket信息并打印
        new Thread(() -> readFromServer(socket)).start();

        while (true) {
            // 输入消息后发送给服务端
            System.out.println("输入你要发送的聊天消息");
            System.out.println("id:message,例如,1:hello 代表向id为1的用户发送hello消息");
            System.out.println("id=0代表向所有人发送消息，例如，0:hello 代表向所有在线用户发送hello");

            String line = userInput.nextLine();

            if (!line.contains(":")) {
                System.err.println("输入格式不对");
            } else {
                int colonIndex = line.indexOf(':');
                int id = Integer.parseInt(line.substring(0, colonIndex));
                String message = line.substring(colonIndex + 1);

                String json = JSON.toJSONString(new Message(id, message));

                Util.writeMessage(socket, json);
            }
        }
    }

    private static void readFromServer(Socket socket) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
