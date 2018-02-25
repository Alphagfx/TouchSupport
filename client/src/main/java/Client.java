import org.apache.log4j.Logger;

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


public class Client implements Runnable {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Logger logger = Logger.getLogger(Client.class.getName());

    private ObjectOutputStream out;
    private Socket socket = new Socket();
    private ConnectionHandler connectionHandler;

    public static void main(String[] args) {

        executorService.execute(new Client());

    }

    public static void exit() {
        try {
            System.out.println("attempt to shutdown executor");
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info("task interrupted", e);
        } finally {
            if (!executorService.isTerminated()) {
                logger.warn("cancel non-finished tasks");
            }
            executorService.shutdownNow();
            logger.info("shutdown finished");
        }
    }

    private Socket connect() {

        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.info("unknown host");
        }
        int timeout = 10000;

        try {
            socket.connect(new InetSocketAddress(address, 1220), timeout);
        } catch (IOException e) {
            logger.warn("socket exception", e);
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
                    logger.error("output stream exception", e);
                }
                connectionHandler = new ConnectionHandler(socket);
                executorService.execute(connectionHandler);
                logger.info("connected client");
            } else if (line.equals("/exit")) {
                connectionHandler.setListening(false);
                executorService.execute(() -> exit());
                logger.info("terminating");
                break;
            } else if (line.equals("/disconnect")) {
                logger.info("disconnecting");
                disconnect();
            } else if (out != null) {
                try {
                    out.writeObject(new Message(line));
                } catch (IOException e) {
                    logger.warn("message sending exception", e);
                }
            }
        }
    }

    public void disconnect() {
        try {
            out.close();
            socket.close();
            logger.info("connections closed");
        } catch (IOException e) {
            logger.warn("disconnect exception", e);
        }
    }

}