import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(1220)) {

            int i = 1;
            ExecutorService executorService = Executors.newCachedThreadPool();

            while (true) {
                Socket client = serverSocket.accept();
                client.setSoTimeout(10000);

                System.out.println("Spawning client " + i++);

                new ObjectOutputStream(client.getOutputStream()).writeObject(new Message("hello from server"));

                executorService.submit(new ConnectionHandler(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
