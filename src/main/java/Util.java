import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;

public class Util {
    public static void writeMessage(Socket socket, String message) throws IOException {
        socket.getOutputStream().write(message.getBytes(Charset.defaultCharset()));
        socket.getOutputStream().write('\n');
        // socket的写是有缓冲的，需要积攒到一定，然后flush冲入
        socket.getOutputStream().flush();
    }
}
