package com.alphagfx.common.connection;

import com.alphagfx.common.Const;
import com.alphagfx.common.IUserDB;
import com.alphagfx.common.Participant;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.concurrent.*;

public class ConnectionHandler {

    private static final Logger logger = Logger.getLogger(ConnectionHandler.class);

    private final InetSocketAddress address;
    private final AsynchronousChannelGroup group;

    private final IUserDB userDB;

    private ConnectionHandler(InetSocketAddress address, IUserDB userDB, AsynchronousChannelGroup group) {
        this.address = address;
        this.userDB = userDB;
        this.group = group;
    }

    public static ConnectionHandler create(InetSocketAddress address, IUserDB userDB) {
        Objects.requireNonNull(userDB);

        AsynchronousChannelGroup group = null;
        try {
            group = AsynchronousChannelGroup.withFixedThreadPool(Const.THREADS_PER_GROUP, Executors.defaultThreadFactory());
        } catch (IOException e) {
            logger.error("Failed to create AsyncChannelGroup", e);
        }
        return new ConnectionHandler(address, userDB, group);
    }

    public boolean launchServer() {
        try {
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
            server.bind(address);
            logger.info("Server is listening at " + address.toString());

            server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                @Override
                public void completed(AsynchronousSocketChannel asynchronousSocketChannel, AsynchronousServerSocketChannel serverSocketChannel) {

                }

                @Override
                public void failed(Throwable throwable, AsynchronousServerSocketChannel serverSocketChannel) {

                }
            });
            return true;
        } catch (IOException e) {
            logger.error("Server launch fail", e);
            return false;
        }
    }

    public Participant connect(InetSocketAddress address) {
        try (AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group)) {
            Future<Void> future = channel.connect(address);
            try {
                future.get(Const.CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.info(e);
            } catch (ExecutionException e) {
                logger.error(e);
            } catch (TimeoutException e) {
                logger.warn(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Participant.create(-2, "new_connect");
    }
}
