package com.alphagfx.common;

//import org.apache.commons.lang3.tuple.Pair;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
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
        run2();
    }

    public void run2() {
        SocketChannel channel;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress("127.0.0.1", 8511));

            while (!Thread.interrupted()) {

                selector.select(1000);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isConnectable()) {
                        System.out.println("I am connected to the server");
                        connect(key);
                    }
                    if (key.isWritable()) {
                        write(key);
                    }
                    if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run1() {
        while (listening) {
            System.out.print(" *");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                registerChannels();

                for (SelectionKey key : selector.keys()) {
                    if (((Participant) key.attachment()).getMessagesToSend().size() != 0) {
                        System.out.println("setting to WRITE");
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                }

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


    private void write(SelectionKey key) throws IOException {

        System.out.println("Enter write");

        Message message = ((Participant) key.attachment()).getMessagesToSend().poll();

        if (message != null) {
            System.out.println("Message to write: " + message);

            SocketChannel channel = (SocketChannel) key.channel();

            channel.write(ByteBuffer.wrap(message.toString().getBytes()));
        }

        /*

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
        */
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        try {
            length = channel.read(readBuffer);
        } catch (IOException e) {
            System.out.println("Reading problem, closing connection");
            key.cancel();
            channel.close();
            return;
        }
        if (length == -1) {
            System.out.println("Nothing was read from server");
            channel.close();
            key.cancel();
            return;
        }
        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        System.out.println("Server said: " + new String(buff));

        /*

        System.out.println("Enter read");
        SocketChannel channel = (SocketChannel) key.channel();

        Message message = null;

        int numRead = 0;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);

            System.out.println("Reading: ");
            while (buffer.position() < buffer.limit()) {
                System.out.print(" #");
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

        */

    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

}

class Test {

    String message = "TEST__MESSAGE";
    private Selector selector;

    public void run1() {
        SocketChannel channel;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress("127.0.0.1", 8511));

            while (!Thread.interrupted()) {

                selector.select(1000);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isConnectable()) {
                        System.out.println("I am connected to the server");
                        connect1(key);
                    }
                    if (key.isWritable()) {
                        write1(key);
                    }
                    if (key.isReadable()) {
                        read1(key);
                    }
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            close1();
        }
    }

    private void close1() {
        try {
            selector.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void read1(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        try {
            length = channel.read(readBuffer);
        } catch (IOException e) {
            System.out.println("Reading problem, closing connection");
            key.cancel();
            channel.close();
            return;
        }
        if (length == -1) {
            System.out.println("Nothing was read from server");
            channel.close();
            key.cancel();
            return;
        }
        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        System.out.println("Server said: " + new String(buff));
    }

    private void write1(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap(message.getBytes()));

        // lets get ready to read.
        key.interestOps(SelectionKey.OP_READ);
    }

    private void connect1(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE);
    }
}

class ConnectionHandler1 implements Runnable {

    private Selector selector;

    private Queue<Pair<Participant, SocketChannel>> pendingChannels = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {

        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            register();


        }

    }

    public void register(Participant participant, SocketChannel channel) {
        pendingChannels.offer(Pair.of(participant, channel));
    }

    private void register() {
        for (Pair pair : pendingChannels) {
            Participant participant = ((Participant) pair.getKey());
            SocketChannel channel = ((SocketChannel) pair.getValue());
            try {
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_WRITE, participant);
            } catch (IOException e) {
//                TODO import logger
                e.printStackTrace();
            }
        }
        pendingChannels.clear();
    }

    private void read(SelectionKey key) {

    }

    private void write(SelectionKey key) {

    }
}

