import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable {

    private Socket socket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Client(Socket socket) {
        this.socket = socket;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);

        Socket socket = null;

        while (true) {
            String line = in.nextLine();
            System.out.println("You wrote this line: " + line);
            if (line.equals("exit")) {
                return;
            }
            if (line.equals("connect")) {
                socket = connect();
                System.out.println("connected client");
            }
            if (socket != null) {
                break;
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(new Client(socket));
        executor.submit(new ClientHandler(socket));
    }

    private static Socket connect() throws IOException {

        InetAddress address = InetAddress.getLocalHost();
        int timeout = 10000;

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, 1220), timeout);

        return socket;

    }

    public void run() {

        Scanner input = new Scanner(System.in);

        while (true) {
            String line = input.nextLine();
            if (line.equals("/exit")) {
                break;
            }
            try {
                out.writeObject(new Message(line));
            } catch (IOException e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
        try {
            in.close();
            out.close();
            socket.close();
            System.out.println("connections closed");
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }

    }

}

class Receiver implements Runnable {

    private Socket socket;

    Receiver(Socket socket) {
        this.socket = socket;
        System.out.println("hello receiver");
    }

    @Override
    public void run() {
        System.out.println("run me");
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Message message = (Message) in.readObject();
                System.out.println(message.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
