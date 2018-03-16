package com.alphagfx.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.*;

public class ConnectionHandlerAsync implements Runnable {

    private final InetSocketAddress address;
    private AsynchronousChannelGroup group;

    private Map<Integer, Participant> usersConnected;

    private IProcessor processor;

    public ConnectionHandlerAsync(InetSocketAddress address, Map<Integer, Participant> usersConnected, IProcessor processor) {
        this.address = address;
        this.usersConnected = usersConnected;
        this.processor = processor;
        try {
            group = AsynchronousChannelGroup.withFixedThreadPool(Const.THREADS_PER_GROUP, Executors.defaultThreadFactory());
        } catch (IOException e) {
            // TODO: 02/03/18 replace with logger
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }

    // TODO: 07/03/18 probably merge with client executor service? proper logging
    public void exit() {
        try {
            System.out.println("attempt to shutdown executor");
            group.shutdown();
            group.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
//            logger.info("task interrupted", e);
            System.out.println("grouo shutdown interrupted");
        } finally {
            if (!group.isTerminated()) {
                System.out.println("cancel non-finished tasks");
//                logger.warn("cancel non-finished tasks");
            }
            try {
                group.shutdownNow();
            } catch (IOException e) {
                System.out.println("Something crazy went wrong");
                e.printStackTrace();
            }
            System.out.println("group shutdown finished");
//            logger.info("shutdown finished");
        }
    }

    public void launchServer() {
        try {
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
            server.bind(address);
            System.out.format("Server is listening at %s%n", address);

            Participant accept = new Participant();
            accept.setName("ACCEPT");
            accept.setId(-1);
            accept.server = server;

            server.accept(accept, new ConnectionHandl());
        } catch (IOException e) {
            // TODO: 03/03/18 logger
            e.printStackTrace();
        }
    }

    public AsynchronousSocketChannel connect(SocketAddress address) {

        AsynchronousSocketChannel channel = null;
        try {
            channel = AsynchronousSocketChannel.open(group);
            Future<Void> result = channel.connect(address);

            // TODO: 07/03/18 replace catch clauses with loggers/messages
            try {
                result.get(Const.CONNECTION_TIMEOUT, TimeUnit.SECONDS);
                System.out.println("Connected");
            } catch (InterruptedException e) {
                System.out.println("Connection interrupted");
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.out.println("Connection is unreachable");
                e.printStackTrace();
                //probably should add
                //throw e;
            }
        } catch (IOException e) {
            // TODO: 07/03/18 logger
            System.out.println("AsyncChannel open failure");
            e.printStackTrace();
        }

        return channel;
    }

    static class WriteHandler implements CompletionHandler<Integer, Participant> {

        // singleton because we are not going to do anything special with this
        private static final WriteHandler handler = new WriteHandler();

        private WriteHandler() {
        }

        public static WriteHandler getHandler() {
            return handler;
        }

        @Override
        public void completed(Integer integer, Participant user) {
            if (user.getMessagesToSend().size() > 0) {
                user.writeMessage(user.getMessagesToSend().poll());
            }
        }

        @Override
        public void failed(Throwable throwable, Participant user) {
            // TODO: 05/03/18 logger
            System.err.println("Failed to write to user");
            throwable.printStackTrace();
        }
}

class ConnectionHandl implements CompletionHandler<AsynchronousSocketChannel, Participant> {

    @Override
    public void completed(AsynchronousSocketChannel client, Participant user) {
        try {
            System.out.format("Accepted a connection from %s%n", client.getRemoteAddress());

            ReadHandler readHandler = new ReadHandler();
            Participant newUser = new Participant();

            newUser.server = user.server;
            newUser.client = client;
            newUser.buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);

            // TODO: 05/03/18 add to the list of current users
            usersConnected.put(newUser.getId(), newUser);
            client.read(newUser.buffer, newUser, readHandler);

            //  using this one instance for handling input connections
            user.server.accept(user, this);
        } catch (IOException e) {
            // TODO: 03/03/18 logger
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable throwable, Participant user) {
        // TODO: 03/03/18 logger
        System.err.println("Failed to accept connection");
        throwable.printStackTrace();
    }
}

class ReadHandler implements CompletionHandler<Integer, Participant> {

    @Override
    public void completed(Integer result, Participant user) {
        if (result == -1) {
            try {
                user.client.close();
                System.err.println("Stopped listening client: " + user.getId());
            } catch (IOException e) {
                // TODO: 03/03/18 logger
                e.printStackTrace();
            }
            return;
        }

        user.buffer.flip();
        int command = user.buffer.getInt();
        int address = user.buffer.getInt();

        byte[] bytes = new byte[user.buffer.limit() - Integer.BYTES * 2];

        user.buffer.get(bytes);

        // decoding message
        Charset charset = Charset.forName(Const.CHARSET);
        Message message = new Message(command, address, new String(bytes, charset));

        // TODO: 05/03/18 put message to list
        user.getMessagesToReceive().offer(message);
//        System.out.println("Other side says: " + message);

        processor.process(message, user);

        user.buffer.clear();

        // relaunching listener automatically
        user.client.read(user.buffer, user, this);
    }

    @Override
    public void failed(Throwable throwable, Participant user) {
        // TODO: 05/03/18 logger
        System.err.println("Failed to read user");
        throwable.printStackTrace();
    }
}
}
