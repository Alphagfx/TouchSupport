package com.alphagfx.common.connection;

import com.alphagfx.common.Const;
import com.alphagfx.common.Participant;
import com.alphagfx.common.Processor;
import com.alphagfx.common.UserDatabase;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

public class ConnectionHandlerAsync {

    private Logger logger = Logger.getLogger(ConnectionHandlerAsync.class);

    private final InetSocketAddress address;
    private AsynchronousChannelGroup group;

    private UserDatabase userDB;
    private UserDatabase connectedUsers;

    private Processor processor;

    private ConnectionHandlerAsync(InetSocketAddress address, UserDatabase connectedUsers, UserDatabase userDB,
                                   Processor processor) {
        this.address = address;
        this.connectedUsers = connectedUsers;
        this.userDB = userDB;
        this.processor = processor;

        initialize();
    }

    public static ConnectionHandlerAsync create(InetSocketAddress address, UserDatabase connectedUsers,
                                                UserDatabase userDB, Processor processor) {
        return new ConnectionHandlerAsync(address, connectedUsers, userDB, processor);
    }

    private void initialize() {
        try {
            group = AsynchronousChannelGroup.withFixedThreadPool(Const.THREADS_PER_GROUP, Executors.defaultThreadFactory());
        } catch (IOException e) {
            logger.error("AsyncChannelGroup creation error", e);
        }
    }

    // TODO: 07/03/18 probably merge with client executor service?
    // stole it
    public void exit() {
        try {
            System.out.println("Attempt to shutdown executor");
            group.shutdown();
            group.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info("Task interrupted", e);
        } finally {
            if (!group.isTerminated()) {
                logger.warn("Cancel non-finished tasks");
            }
            try {
                group.shutdownNow();
            } catch (IOException e) {
                logger.error("Something crazy went wrong", e);
            }
            logger.info("Group shutdown finished");
        }
    }

    public void launchServer() {
        try {
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
            server.bind(address);
            server.accept(server, new ConnectionHandl(this));

            logger.info("Server is listening at " + address);
        } catch (IOException e) {
            logger.error("Fail while launching server", e);
        }
    }

    // TODO: 24/05/18 Remove public access
    public AsynchronousSocketChannel connect(SocketAddress address) {

        AsynchronousSocketChannel channel = null;
        try {
            channel = AsynchronousSocketChannel.open(group);
            Future<Void> result = channel.connect(address);

            try {
                result.get(Const.CONNECTION_TIMEOUT, TimeUnit.SECONDS);
                logger.info("Connected");
            } catch (InterruptedException e) {
                logger.warn("Connection interrupted", e);
            } catch (ExecutionException e) {
                logger.error("Problem while connecting", e);
            } catch (TimeoutException e) {
                logger.warn("Connection is unreachable", e);
            }
        } catch (IOException e) {
            logger.warn("AsyncChannel open failure", e);
        }

        return channel;
    }

    void newUserConnected(AsynchronousSocketChannel client) {
        try {
            logger.info("Accepted a connection from " + client.getRemoteAddress());

            // TODO: 24/05/18 Make proper read of user properties
            Participant newUser = Participant.create(-1, "new");

            ReadHandler readHandler = ReadHandler.create(processor);
            ByteBuffer buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);

//            AttachmentImpl attachment = new AttachmentImpl.Builder().setClient(client).setBuffer(buffer).
//                    setRead(readHandler).setWrite(WriteHandler.getHandler()).setUser(newUser).build();

            AttachmentImpl attachment = AttachmentImpl.create(client, readHandler, WriteHandler.getHandler());

            // FIXME: 31/05/18 here should be more convenient id number
            connectedUsers.put(newUser.getId(), newUser);

            attachment.read();

            // FIXME: 04/06/18 id should be gotten through login service, database or smth similar
            int id = 0;
            userDB.updateUserData(id, newUser);
        } catch (IOException e) {
            logger.error("Error happened while creating new user", e);
        }
    }
}
