package com.alphagfx.server;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServer {

    private static Logger logger = Logger.getLogger(SimpleServer.class);

    private MessageProcessor processor;
    private ConnectionHandler server;

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private TerminalReader input = new TerminalReader();

    protected SimpleServer(MessageProcessor processor, ConnectionHandler server) {
        Objects.requireNonNull(processor);
        Objects.requireNonNull(server);

        this.processor = processor;
        this.server = server;
    }

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("server.xml");
        SimpleServer server = (SimpleServer) context.getBean("server");

        server.startServer();
        server.stopServer();
    }

    public void startServer() {
//        executor.submit(processor);

        server.start();
        logger.info("Server has been launched");
        input.read();
    }

    public void stopServer() {
        executor.shutdown();
        server.stop();
    }

    private class TerminalReader {

        private boolean alive = true;
        private Scanner scanner = new Scanner(System.in);

        private void read() {
            while (alive) {
                readInput();
            }
        }

        private void readInput() {
            String line = scanner.nextLine();
            if ("exit".equalsIgnoreCase(line)) {
                alive = false;
            }
        }

    }



}
