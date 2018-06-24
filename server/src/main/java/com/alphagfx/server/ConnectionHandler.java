package com.alphagfx.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConnectionHandler {
    private static Logger logger = Logger.getLogger(ConnectionHandler.class);

    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel server;

    private SocketAddress address;

    private Factory<CompletionHandler> handlersFactory;

    public ConnectionHandler(SocketAddress address, Factory<CompletionHandler> handlesFactory) {
        this.address = address;
        this.handlersFactory = handlesFactory;
    }

    public void start() {
        startServer();
        acceptConnections();
        logger.info("Successful launch");
    }

    public void stop() {
        try {
            group.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Server stop interrupted", e);
        }
    }

    private void startServer() {
        try {
            group = AsynchronousChannelGroup.withFixedThreadPool(5, Executors.defaultThreadFactory());
        } catch (IOException e) {
            logger.fatal("Group start fail", e);
        }

        try {
            server = AsynchronousServerSocketChannel.open(group);
            server.bind(address);
        } catch (IOException e) {
            logger.error("Failed to open/bind server", e);
        }

    }

    private void acceptConnections() {
        CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> handler = handlersFactory.getAccept();
        server.accept(server, handler);
    }

}
