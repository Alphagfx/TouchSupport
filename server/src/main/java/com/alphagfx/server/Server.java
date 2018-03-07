package com.alphagfx.server;

import com.alphagfx.common.Chat;
import com.alphagfx.common.ConnectionManager;
import com.alphagfx.common.Message;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static Logger logger = Logger.getLogger(Server.class.getName());


    public static void main1(String[] args) {
//
//        com.alphagfx.common.ConnectionHandler connectionHandler = new com.alphagfx.common.ConnectionHandler();

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLocalHost(), 8888));
            serverSocketChannel.configureBlocking(false);
            int i = 0;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
//            executorService.execute(connectionHandler);

            Chat chat = new Chat();

            while (true) {
                SocketChannel client = serverSocketChannel.accept();
                if (client != null) {
                    System.out.println("hello client");
//                    connectionHandler.addChannelServer(chat.addParticipant(i++, client.getLocalAddress()), client);
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

    public static void main(String[] args) throws UnknownHostException {

        Executor executor = Executors.newCachedThreadPool();

        ServerProcessor processor = new ServerProcessor();

        ConnectionManager manager = new ConnectionManager(processor, new InetSocketAddress(InetAddress.getLocalHost(), 8889));

        manager.addInputConnectionListener(new InetSocketAddress(InetAddress.getLocalHost(), 8890));

        manager.addConnectionHandler();

        executor.execute(manager);


        while (true) {
            processor.process();
        }

    }

    @Override
    public void run() {

    }
}
