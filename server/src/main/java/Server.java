import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(1220)) {

            int i = 1;

            while (true) {
                Socket client = serverSocket.accept();

                System.out.println("Spawning client " + i++);

                new ObjectOutputStream(client.getOutputStream()).writeObject(new Message("hello from server"));

                Runnable r = new ClientHandler(client);
                new Thread(r).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
