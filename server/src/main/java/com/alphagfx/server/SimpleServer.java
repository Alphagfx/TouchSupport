package com.alphagfx.server;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServer {

    private static Logger logger = Logger.getLogger(SimpleServer.class);

    private MessageProcessor processor;
    private ConnectionHandler server;
    private ProcessingQueue<User> processingQueue;
    private Factory<CompletionHandler> completionHandlerFactory;

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private Scanner scanner = new Scanner(System.in);

    private boolean alive = true;

    private SimpleServer(MessageProcessor processor, ProcessingQueue<User> processingQueue, ConnectionHandler server) {
        Objects.requireNonNull(processor);
        Objects.requireNonNull(processingQueue);
        Objects.requireNonNull(server);

        this.processor = processor;
    }

    public static void main(String[] args) {
        ProcessingQueueImpl queue = new ProcessingQueueImpl();
        SimpleServer server = create(new MessageProcessorImpl(queue), queue, new ConnectionHandler(new InetSocketAddress(5000), queue));

        server.launch();
    }

    public static SimpleServer create(MessageProcessor processor, ProcessingQueue<User> processingQueue, ConnectionHandler server) {
        return new SimpleServer(processor, processingQueue, server);
    }

    public void launch() {
        executor.submit(processor);
        server.start();
        act();
    }

    private void act() {
        while (alive) {
            readInput();
        }
        executor.shutdown();
        server.stop();
    }

    private void readInput() {
        String line = scanner.nextLine();
        if ("exit".equalsIgnoreCase(line)) {
            alive = false;
        }
    }


}
