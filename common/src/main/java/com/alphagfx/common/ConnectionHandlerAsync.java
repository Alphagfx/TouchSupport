package com.alphagfx.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

public class ConnectionHandlerAsync implements Runnable {

    private final InetSocketAddress address;
    private final Participant attachment;
    private AsynchronousChannelGroup group;

    public ConnectionHandlerAsync(InetSocketAddress address, Participant attachment) {
        this.attachment = attachment;
        this.address = address;
        try {
            group = AsynchronousChannelGroup.withFixedThreadPool(Const.THREADS_PER_GROUP, Executors.defaultThreadFactory());
        } catch (IOException e) {
            // TODO: 02/03/18 replace with logger
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
            server.bind(address);
            System.out.format("Server is listening at %s%n", address);

            server.accept(attachment, new ConnectionHandl());

        } catch (IOException e) {
            // TODO: 03/03/18 logger
            e.printStackTrace();
        }

    }
}

class ConnectionHandl implements CompletionHandler<AsynchronousSocketChannel, Participant> {

    @Override
    public void completed(AsynchronousSocketChannel client, Participant attachment) {
        try {
            System.out.format("Accepted a connection from %s%n", client.getRemoteAddress());

            ReadHandler readHandler = new ReadHandler();
            Participant newAttachment = new Participant();

            newAttachment.server = attachment.server;
            newAttachment.client = client;
            newAttachment.buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);

            // TODO: 05/03/18 add to the list of current users

            client.read(attachment.buffer, attachment, readHandler);

            //  using this one instance for handling input connections
            attachment.server.accept(newAttachment, this);
        } catch (IOException e) {
            // TODO: 03/03/18 logger
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable throwable, Participant attachment) {
        // TODO: 03/03/18 logger
        System.err.println("Failed to accept connection");
        throwable.printStackTrace();
    }
}

class ReadHandler implements CompletionHandler<Integer, Participant> {

    @Override
    public void completed(Integer result, Participant attachment) {
        if (result == -1) {
            try {
                attachment.client.close();
                System.err.println("Stopped listening client: " + attachment.getId());
            } catch (IOException e) {
                // TODO: 03/03/18 logger
                e.printStackTrace();
            }
            return;
        }

        attachment.buffer.flip();
        byte[] bytes = new byte[attachment.buffer.limit()];

        attachment.buffer.get(bytes, 0, attachment.buffer.limit());

        // decoding message
        Charset charset = Charset.forName(Const.CHARSET);
        String message = new String(bytes, charset);

        // TODO: 05/03/18 put message to list
        attachment.getMessagesToReceive().offer(new Message(0, message));
        System.out.println("Other side says: " + message);

        attachment.buffer.rewind();

        // relaunching listener automatically
        attachment.client.read(attachment.buffer, attachment, this);
    }

    @Override
    public void failed(Throwable throwable, Participant attachment) {
        // TODO: 05/03/18 logger
        System.err.println("Failed to read user");
        throwable.printStackTrace();
    }
}

class WriteHandler implements CompletionHandler<Integer, Participant> {

    @Override
    public void completed(Integer integer, Participant attachment) {
    }

    @Override
    public void failed(Throwable throwable, Participant attachment) {
        // TODO: 05/03/18 logger
        System.err.println("Failed to write to user");
        throwable.printStackTrace();
    }
}
