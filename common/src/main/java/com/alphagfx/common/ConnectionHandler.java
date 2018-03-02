package com.alphagfx.common;

//import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionHandler implements Runnable {

    private static Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    private Selector selector;

    ConnectionManager manager;

    private boolean listening = true;
    private IParticipantProcessor processor;
    private Queue<Pair<Participant, SocketChannel>> pendingChannels = new ConcurrentLinkedQueue<>();

    private ConnectionHandler(ConnectionManager manager, IParticipantProcessor processor) {
        this.processor = processor;
        this.manager = manager;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error("Selector open fail", e);
        }
    }

    public static ConnectionHandler getHandler(ConnectionManager manager, IParticipantProcessor processor) {
        return new ConnectionHandler(manager, processor);
    }

    public void addChannel(Participant participant, SocketChannel channel) {
        pendingChannels.offer(Pair.of(participant, channel));
    }

    private void registerChannels() throws IOException {
        while (pendingChannels.size() != 0) {
            Pair<Participant, SocketChannel> pair = pendingChannels.poll();
            SocketChannel channel = pair.getValue();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ, pair.getKey());
        }
    }


    @Override
    public void run() {
        while (listening) {
            System.out.print(" *");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                registerChannels();


                selector.selectNow();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                for (SelectionKey key : selectionKeys) {

                    System.out.println(selectionKeys);

                    if (key.isValid()) {

                        if (key.isConnectable()) {
                            connect(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                        // Register this channel again
                        Object attachment = key.attachment();
                        key.channel().register(selector, key.interestOps(), attachment);
                    } else {
                        manager.getUsers().remove(key.attachment());
                    }
                }

            } catch (IOException e) {
                logger.info("'read-write' cycle exception", e);
            }
        }

    }

    private void connect(SelectionKey key) {
        System.out.println("Connecting");


    }


    private void write(SelectionKey key) {

        System.out.println("Enter write");

        Queue<Message> messages = ((Participant) key.attachment()).getMessagesToSend();

        while (!messages.isEmpty()) {

            Message message = messages.poll();
            System.out.println("Message to write: " + message);

            SocketChannel channel = (SocketChannel) key.channel();

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);
            buffer.putInt(message.getCommand()).putInt(message.getSize());

            try {

                while (buffer.position() < buffer.limit()) {
                    channel.write(buffer);
                }

                buffer = StandardCharsets.UTF_8.encode(message.getMessage());

                while (buffer.position() < buffer.limit()) {
                    channel.write(buffer);
                }
            } catch (IOException e) {
                logger.warn("Write exception", e);
            }
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {

        System.out.println("Enter read");
        SocketChannel channel = (SocketChannel) key.channel();

        Message message = null;

        int numRead = 0;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);


            while (buffer.position() < buffer.limit()) {
                numRead = channel.read(buffer);
            }

            int command = buffer.asIntBuffer().get();
            int size = buffer.asIntBuffer().get();

            System.out.println("command = " + command + ", size = " + size);

            buffer = ByteBuffer.allocate(size);

            while (buffer.position() < buffer.limit()) {
                numRead = channel.read(buffer);
            }

            message = new Message(command, StandardCharsets.UTF_8.decode(buffer).position(0).toString());
        } catch (IOException e) {
            logger.warn("Forcibly closed connection");
            key.cancel();
        }

        if (message != null) {
            Participant p = ((Participant) key.attachment());
            p.getMessagesToReceive().add(message);
            processor.addParticipant(p);
        }

        if (((Participant) key.attachment()).getMessagesToSend().size() != 0) {
            key.interestOps(SelectionKey.OP_WRITE);
        }

        if (numRead == -1) {
            logger.info("Client disconnected");
            key.channel().close();
            key.cancel();
        }

    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

}
