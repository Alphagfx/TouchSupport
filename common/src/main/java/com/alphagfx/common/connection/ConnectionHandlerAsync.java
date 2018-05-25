package com.alphagfx.common.connection;

import com.alphagfx.common.Const;
import com.alphagfx.common.IProcessor;
import com.alphagfx.common.Participant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

public class ConnectionHandlerAsync implements Runnable {

    private final InetSocketAddress address;
    private AsynchronousChannelGroup group;

    private Map<Integer, Participant> usersConnected;

    private IProcessor processor;

    public ConnectionHandlerAsync(InetSocketAddress address, Map<Integer, Participant> usersConnected, IProcessor processor) {
        this.address = address;
        this.usersConnected = usersConnected;
        this.processor = Objects.nonNull(processor) ? processor : (message, user) -> {
        };
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
            System.out.println("group shutdown interrupted");
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

            server.accept(server, new ConnectionHandl(this));
        } catch (IOException e) {
            // TODO: 03/03/18 logger
            e.printStackTrace();
        }
    }

    // TODO: 24/05/18 Remove public access
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
                System.err.println("Connection interrupted");
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.err.println("Connection is unreachable");
                e.printStackTrace();
            }
        } catch (IOException e) {
            // TODO: 07/03/18 logger
            System.err.println("AsyncChannel open failure");
            e.printStackTrace();
        }

        return channel;
    }

    void newUserConnected(AsynchronousSocketChannel client, AsynchronousServerSocketChannel server) {
        try {
            System.out.format("Accepted a connection from %s%n", client.getRemoteAddress());

            ReadHandler readHandler = ReadHandler.create(processor);
            ByteBuffer buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);

            // TODO: 24/05/18 Make proper read of user properties
            Participant newUser = Participant.create(-1, "new");

            Attachment attachment = new Attachment.Builder().setClient(client).setServer(server).setBuffer(buffer).
                    setRead(readHandler).setWrite(WriteHandler.getHandler()).setUser(newUser).build();

            usersConnected.put(newUser.getId(), newUser);

            attachment.readChannelWithHandler();
        } catch (IOException e) {
            // TODO: 03/03/18 logger
            e.printStackTrace();
        }
    }
}
