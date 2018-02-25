import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            int i = 1;
            ExecutorService executorService = Executors.newFixedThreadPool(10);

            while (true) {
                SocketChannel client = serverSocketChannel.accept();
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                System.out.println("Spawning client " + i++);

                client.write(buffer);

                executorService.submit(new ConnectionHandler(client));
            }
        } catch (IOException e) {
            logger.error("Server problem : ", e);
        }
    }
}
