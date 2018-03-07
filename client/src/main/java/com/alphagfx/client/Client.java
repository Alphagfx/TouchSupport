package com.alphagfx.client;

import com.alphagfx.common.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.*;


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
        runAnother();
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
//                    com.alphagfx.common.Chat.com.alphagfx.common.Participant participant = chat.addParticipant(0, new InetSocketAddress(InetAddress.getLocalHost(), 8888));
                    System.out.println("added channel");
                    chat.sendMessageTo(0, new Message(0, "hello server"));
                    throw new IOException();
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

    private void runIt() throws UnknownHostException {
        Scanner input = new Scanner(System.in);

        ConnectionManager manager = null;

        Chat chat = new Chat();

        String line = "";

        while (!line.equals("/exit")) {

            line = input.nextLine();

            if (line.equals("/connect")) {
                logger.info("Establishing connection");
                manager = new ConnectionManager(new ClientProcessor(), new InetSocketAddress(InetAddress.getLocalHost(), 8888));
                manager.addConnectionHandler();
                manager.openConnection(new InetSocketAddress(InetAddress.getLocalHost(), 8889));
                executorService.execute(manager);
            } else if (line.equals("/c chat")) {
                System.out.println("/c chat typed in");
                if (manager != null) {
                    System.out.println(manager.getUsers());
                    manager.getUsers().forEach((id, p) -> {
                        chat.addParticipant(p);
                    });
                }
            } else {
                chat.sendEveryone(new Message(0, line));
                System.out.println(new Message(0, line));
            }
        }

    }

    // TODO: 07/03/18 replace run()
    private void runAnother() {
        Participant user = new Participant();

        InetSocketAddress address = new InetSocketAddress("localhost", 8888);
        ConnectionHandlerAsync handler = new ConnectionHandlerAsync(address, user);
        executorService.execute(handler);

        AsynchronousSocketChannel channel = null;
        try {
            channel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SocketAddress serverAddr = new InetSocketAddress("localhost", 8989);
        Future<Void> result = channel.connect(serverAddr);
        try {
            result.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Connected");

        user.client = channel;

        Scanner input = new Scanner(System.in);
        String line = "";

        while (!line.equals("/exit")) {
            line = input.nextLine();

            user.writeMessage(line);
        }

        exit();

    }
}