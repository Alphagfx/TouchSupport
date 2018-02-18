import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Logger logger = Logger.getLogger("client");

    private ObjectOutputStream out;
    private Socket socket = new Socket();
    private ConnectionHandler connectionHandler;

    public static void main(String[] args) {

        executorService.execute(new Client());
        try {
            logger.addHandler(new FileHandler("client.log"));
        } catch (IOException e) {
            System.err.println("No access to the internal disk storage");
            e.printStackTrace();
        }

    }

    public static void exit() {
        try {
            System.out.println("attempt to shutdown executor");
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.FINE, "task interrupted", e);
        } finally {
            if (!executorService.isTerminated()) {
                logger.log(Level.FINE, "cancel non-finished tasks");
            }
            executorService.shutdownNow();
            logger.log(Level.INFO, "shutdown finished");
        }
    }

    private Socket connect() {

        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.log(Level.FINE, "unknown host", e);
        }
        int timeout = 10000;

        try {
            socket.connect(new InetSocketAddress(address, 1220), timeout);
        } catch (IOException e) {
            logger.log(Level.WARNING, "socket exception", e);
        }

        return socket;

    }

    public void run() {

        Scanner input = new Scanner(System.in);

        while (true) {
            String line = input.nextLine();
            if (line.equals("/connect")) {
                socket = connect();
                try {
                    out = new ObjectOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "output stream exception", e);
                }
                connectionHandler = new ConnectionHandler(socket);
                executorService.execute(connectionHandler);
                logger.log(Level.FINEST, "connected client");
            } else if (line.equals("/exit")) {
                connectionHandler.setListening(false);
                executorService.execute(() -> exit());
                logger.log(Level.FINEST, "terminating");
                break;
            } else if (line.equals("/disconnect")) {
                logger.log(Level.FINEST, "disconnecting");
                disconnect();
            } else if (out != null) {
                try {
                    out.writeObject(new Message(line));
                } catch (IOException e) {
                    logger.log(Level.WARNING, "message sending exception", e);
                }
            }
        }
    }

    public void disconnect() {
        try {
            out.close();
            socket.close();
            logger.log(Level.FINEST, "connections closed");
        } catch (IOException e) {
            logger.log(Level.WARNING, "disconnect exception", e);
        }
    }

}