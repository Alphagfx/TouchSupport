package com.alphagfx.server;

import org.apache.log4j.Logger;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessingQueueImpl<T> implements ProcessingQueue<User>, Factory<CompletionHandler> {

    private static Logger logger = Logger.getLogger(ProcessingQueueImpl.class);

    private Queue<User> queue = new ConcurrentLinkedQueue<>();

    @Override
    public boolean add(User t) {
        return queue.add(t);
    }

    @Override
    public User poll() {
        return queue.poll();
    }


    @Override
    public CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> getAccept() {
        return new AcceptHandler();
    }

    @Override
    public CompletionHandler getRead() {
        return new ReadHandler();
    }

    @Override
    public CompletionHandler getWrite() {
        return null;
    }


    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

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
            channel.read(user.buffer, user, new ReadHandler());
        }

    }

    private class ReadHandler implements CompletionHandler<Integer, User> {

        @Override
        public void completed(Integer bytesRead, User user) {
            if (bytesRead > 0) {
                ProcessingQueueImpl.this.add(user);
            }
            user.connection.read(user.buffer, user, this);
        }

        @Override
        public void failed(Throwable e, User user) {
            logger.warn("Fail reading channel", e);
        }
    }
}
