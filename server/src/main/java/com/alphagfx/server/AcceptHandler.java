package com.alphagfx.server;

import org.apache.log4j.Logger;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
    private static org.apache.log4j.Logger logger = Logger.getLogger(AcceptHandler.class);

    private Factory<CompletionHandler> handlers;

    public AcceptHandler(Factory handlers) {
        this.handlers = handlers;
    }

    @Override
    public void completed(AsynchronousSocketChannel newConnection, AsynchronousServerSocketChannel server) {
        logger.info("Connection accepted");

        readConnection(newConnection);
        server.accept(server, this);
    }

    @Override
    public void failed(Throwable e, AsynchronousServerSocketChannel server) {
        logger.warn("Failed to accept connection", e);
    }

    private void readConnection(AsynchronousSocketChannel channel) {
        User user = new User(channel);
        channel.read(user.buffer, user, handlers.getRead());
    }

}
