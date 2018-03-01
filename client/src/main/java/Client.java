import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Client implements Runnable {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Logger logger = Logger.getLogger(Client.class.getName());

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

    @Override
    public void run() {
        runMe();
    }

    private void runMe() {
        Scanner input = new Scanner(System.in);

        Chat chat = new Chat();


        String line = "";
        while (!line.equals("/exit")) {

            if (chat.readEveryone() == null) {
                System.out.println("input is null");
            }

            line = input.nextLine();

            if (line.equals("/connect")) {
                try {
                    logger.info("Connecting");
                    Chat.Participant participant = chat.addParticipant(0, new InetSocketAddress(InetAddress.getLocalHost(), 8888));
                    System.out.println("added channel");
                    chat.sendMessageTo(0, new Message(0, "hello server"));
                } catch (IOException e) {
                    logger.warn("failed to open channel", e);
                } catch (NullPointerException e) {
                    logger.error("Null pointer", e);
                }
            } else {
                chat.sendMessageTo(0, new Message(0, line));
            }
        }
        connectionHandler.setListening(false);
    }

    public ConnectionHandler launch() {
        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 8888);
            connectionHandler = ConnectionHandler.getHandler(address);
            executorService.execute(connectionHandler);
        } catch (UnknownHostException e) {
            logger.warn("Unknown host", e);
        }
    }

}