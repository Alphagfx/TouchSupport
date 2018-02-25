import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ConnectionHandler implements Runnable {

    private static Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    private Selector selector;

    private boolean listening = true;

    public ConnectionHandler() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error("selector open fail", e);
        }
    }

    public Chat.Participant addChannel(Chat.Participant participant) throws IOException {
        SocketChannel channel = SocketChannel.open(participant.getAddress());
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ).attach(participant);
        channel.register(selector, SelectionKey.OP_WRITE).attach(participant);
        selector.wakeup();
//        channel.setOption(SocketOptions.SO_KEEPALIVE, true);
        return participant;
    }

    public Chat.Participant addChannelServer(Chat.Participant participant, SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ).attach(participant);
        socketChannel.register(selector, SelectionKey.OP_WRITE).attach(participant);
        selector.wakeup();
        return participant;
    }

//    public Chat.Participant addChannel

    @Override
    public void run() {
//        System.out.println("this shit is running");
        while (listening) {
//            System.out.println("before sleep");
            try {
                Thread.sleep(100);
//                System.out.println("ended sleep");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
//                System.out.println("entered select");
                selector.selectNow();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

//                System.out.println("before selection keys");
                for (SelectionKey key : selectionKeys) {
//                    System.out.println(""+key.attachment().toString());
//                    System.out.println("channel inner cycle");
                    if (key.isValid()) {
//                        System.out.println("before reading: "+key.attachment().toString());

                        System.out.println("before readable");
                        if (key.isReadable()) {
                            System.out.println("inside readable");
                            SocketChannel channel = (SocketChannel) key.channel();

                            System.out.println("after socket");
                            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);

                            while (buffer.position() < buffer.limit()) {
                                channel.read(buffer);
                            }

                            int command = buffer.asIntBuffer().get();
                            int size = buffer.asIntBuffer().get();

                            System.out.println("command = " + command + ", size = " + size);

                            buffer = ByteBuffer.allocate(size);

                            while (buffer.position() < buffer.limit()) {
                                channel.read(buffer);
                            }

                            Message message = new Message(command, StandardCharsets.UTF_8.decode(buffer).position(0).toString());
                            System.out.println("Message read: " + message);

                            ((Chat.Participant) key.attachment()).getMessagesToReceive().add(message);
//                            System.out.println("ended reading");

                        }
//                        System.out.println("before writing: "+key.attachment().toString());


                        if (key.isWritable()) {
//                            if (key.attachment() == null) {
//                                System.out.println("LOST ATTACHMENT");
//                            }
                            Message message = ((Chat.Participant) key.attachment()).getMessagesToSend().poll();
                            if (message == null) {
//                                System.out.println("no messages");
                                continue;
                            }

                            System.out.println("Message to write: " + message);

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
//                            System.out.println("ended writing");

                        }
//                        System.out.println("before register: "+key.attachment().toString());


                        // Register this channel again
                        Object attachment = key.attachment();
                        SelectionKey selectionKey = key.channel().register(selector, key.interestOps());
//                        if (attachment == null) {
//                            System.out.println("STILL NO ATTACHMENT");
//                        }
                        selectionKey.attach(attachment);
//                        System.out.println("registered channel again");
                    }
                }

            } catch (IOException e) {
                logger.error("'read-write' cycle exception", e);
            }
        }

    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

}
