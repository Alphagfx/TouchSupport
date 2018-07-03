package com.alphagfx.server;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

class ReadWriteHandler<T> implements CompletionHandler<Integer, User> {

    private static Logger logger = Logger.getLogger(ReadWriteHandler.class);

    private MessageProcessor messageProcessor;
    private boolean write = false;
    private boolean read = true;

    private int completed = 0;

    ReadWriteHandler(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void completed(Integer bytesTreated, User user) {

        System.err.println(Thread.currentThread());

        if (read) {
            if (bytesTreated == -1) {
                closeChannel();
            } else if (bytesTreated == 0) {
                doNothing();
            } else {
                readMessage();
                messageProcessor.process(user);
            }
            if (user.hasItemsToWrite()) {
                System.err.println("Writing to the channel");
                user.buffer.clear();
                ByteBuffer buffer = user.writeQueue.remove();
                user.buffer.put(buffer);
                user.buffer.flip();

                read = false;
                user.connection.write(user.buffer, user, this);
            } else {
                user.buffer.clear();
                read = true;

                user.connection.read(user.buffer, user, this);
            }
        } else {
            user.buffer.clear();
            read = true;

            user.connection.read(user.buffer, user, this);
        }


    }

    private void readMessage() {
        logger.info("Reading the channel");
    }

    private void doNothing() {
        logger.info("Channel is procrastinating");
    }

    private void closeChannel() {
        logger.info("This channel read equals -1 and the channel should be closed");
    }

    @Override
    public void failed(Throwable e, User user) {
        logger.warn("Channel read error", e);
    }
}
