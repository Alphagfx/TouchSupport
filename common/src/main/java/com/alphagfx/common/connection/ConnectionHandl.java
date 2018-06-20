package com.alphagfx.common.connection;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ConnectionHandl implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

    private ConnectionHandlerAsync handlerAsync;

    ConnectionHandl(ConnectionHandlerAsync handlerAsync) {
        this.handlerAsync = handlerAsync;
    }

    @Override
    public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel server) {
        handlerAsync.newUserConnected(client);

        //  using this one instance for handling input connections
        server.accept(server, this);
    }

    @Override
    public void failed(Throwable throwable, AsynchronousServerSocketChannel server) {
        // TODO: 03/03/18 logger
        System.err.println("Failed to accept connection");
        throwable.printStackTrace();
    }
}
