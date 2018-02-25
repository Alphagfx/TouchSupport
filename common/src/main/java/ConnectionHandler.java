import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ConnectionHandler implements Runnable {

    private static Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    private SocketChannel socketChannel;

    private LinkedList<Message> outMessages;
    private LinkedList<Message> inMessages;

    private Selector selector;

    private boolean listening = true;

    public ConnectionHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        outMessages = ((LinkedList<Message>) Collections.synchronizedList(new LinkedList<Message>()));
        inMessages = ((LinkedList<Message>) Collections.synchronizedList(new LinkedList<Message>()));
    }

    @Override
    public void run() {

        try {
            selector = Selector.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            socketChannel.register(selector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            logger.error("Open selector or register channel exception", e);
        }

        while (listening) {
            try {
                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                for (SelectionKey key : selectionKeys) {

                    if (key.isValid()) {

                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();

                            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);

                            while (buffer.position() < buffer.limit()) {
                                channel.read(buffer);
                            }

                            int command = buffer.asIntBuffer().get();
                            int size = buffer.asIntBuffer().get();

                            buffer = ByteBuffer.allocate(size);

                            while (buffer.position() < buffer.limit()) {
                                channel.read(buffer);
                            }

                            inMessages.add(new Message(command, StandardCharsets.UTF_8.decode(buffer).position(0).toString()));
                        }

                        if (key.isWritable()) {
                            Message message = outMessages.poll();
                            if (message == null) {
                                key.channel().register(selector, SelectionKey.OP_WRITE);
                                continue;
                            }

                            SocketChannel channel = (SocketChannel) key.channel();

                            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);
                            buffer.putInt(message.getCommand()).putInt(message.getSize());

                            while (buffer.position() < buffer.limit()) {
                                channel.write(buffer);
                            }

                            buffer = StandardCharsets.UTF_8.encode(message.getMessage());

                            while (buffer.position() < buffer.limit()) {
                                channel.write(buffer);
                            }
                        }

                        // Register this channel again
                        key.channel().register(selector, key.interestOps()).attach(key.attachment());
                    }
                }

            } catch (IOException e) {
                logger.error("'read-write' cycle exception", e);
            }
        }

//
//        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
//
//            while (isListening()) {
//                try {
//                    Message message = ((Message) in.readObject());
//                    System.out.println(message.getMessage());
//                } catch (EOFException messageFail) {
//                    System.err.println(messageFail);
//                }
//            }
//
//        } catch (SocketException e) {
////            System.out.println("0");
////            System.err.println(e);
//        } catch (IOException e) {
////            System.out.println("1");
////            System.err.println(e);
//        } catch (ClassNotFoundException e) {
////            System.out.println("2");
////            System.err.println(e);
//        }

    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public void sendMessages(List<Message> messages) {
        outMessages.addAll(messages);
    }

    //    should be thread-safe
    public List<Message> getMessages() {
        List<Message> result = inMessages.subList(0, inMessages.size());
        inMessages.removeAll(result);
        return result;
    }
}
