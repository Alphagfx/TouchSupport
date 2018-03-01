import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.Set;

public class ConnectionHandler implements Runnable {

    private static Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    private Selector selector;

    private boolean listening = true;

    private ConnectionHandler() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error("Selector open fail", e);
        }
    }

    public static ConnectionHandler getHandler() {
        return new ConnectionHandler();
    }

    public Chat.Participant addChannel(Chat.Participant participant) throws IOException {
        SocketChannel channel = SocketChannel.open(participant.getAddress());
        channel.configureBlocking(false);
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
        key.attach(participant);
        System.out.println("selector wakeup");
        return participant;
    }



    @Override
    public void run() {
        while (listening) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Before select");
                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                System.out.println(selectionKeys.toString());

                for (SelectionKey key : selectionKeys) {

//                    if (false && key.attachment() == null) {
//                        logger.warn("Null instead of Chat Participant");
//                        System.out.println("null participant");
//                        key.channel().close();
//                        key.cancel();
//                    }
//
//                    if (key.attachment() != null && !((Chat.Participant) key.attachment()).getMessagesToSend().isEmpty()) {
//                        System.out.println("before interest ops");
//                        key.interestOps(SelectionKey.OP_WRITE);
//                        System.out.println("after interest ops");
//                    }

                    if (key.isValid()) {
                        System.out.println("key is valid");

                        if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                        // Register this channel again
                        Object attachment = key.attachment();
                        key.channel().register(selector, SelectionKey.OP_READ, attachment);
                    }
                }

            } catch (IOException e) {
                logger.info("'read-write' cycle exception", e);
            }
        }

    }


    private void write(SelectionKey key) {

        System.out.println("Enter write");

        Queue<Message> messages = ((Chat.Participant) key.attachment()).getMessagesToSend();

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
            ((Chat.Participant) key.attachment()).getMessagesToReceive().add(message);
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
