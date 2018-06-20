package com.alphagfx.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

public class SimpleServer {

    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel server;

    public static void main(String[] args) {
        new SimpleServer().startServer();
    }

    private void startServer() {
        try {
            group = AsynchronousChannelGroup.withFixedThreadPool(5, Executors.defaultThreadFactory());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(5000));
        } catch (IOException e) {
            e.printStackTrace();
        }

        acceptConnection();
    }

    private void acceptConnection() {

        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel asynchronousSocketChannel, Void aVoid) {
                System.out.println("Connection accepted");
            }

            @Override
            public void failed(Throwable throwable, Void aVoid) {

            }
        });

    }
}
