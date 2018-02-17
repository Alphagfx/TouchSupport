import sun.net.ConnectionResetException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            while (true) {

                Message message = ((Message) in.readObject());
                System.out.println(message.getMessage());
                out.writeObject(new Message("Got your message"));
            }

        } catch (ConnectionResetException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.out.println("1");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("2");
            e.printStackTrace();
        }

    }
}
