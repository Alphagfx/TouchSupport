import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {

        ConnectionHandler connectionHandler = new ConnectionHandler();

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLocalHost(), 8888));
            serverSocketChannel.configureBlocking(false);
            int i = 0;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            executorService.execute(connectionHandler);

            Chat chat = new Chat();

            while (true) {
                SocketChannel client = serverSocketChannel.accept();
                if (client != null) {
                    System.out.println("hello client");
                    connectionHandler.addChannelServer(chat.addParticipant(i++, client.getLocalAddress()), client);
                    System.out.println("Spawning client " + i++);
                }

                chat.readEveryone().forEach(System.out::println);
                chat.checkParticipants().forEach((System.out::println));
                chat.sendEveryone(new Message(0, "Timestamp from server: " + new Timestamp(Calendar.getInstance().getTime().getTime()).toString()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("sleep interrupt");
                }
            }
        } catch (IOException e) {
            logger.error("Server problem : ", e);
        }
    }
}
