package com.alphagfx.server;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

class ReadWriteHandler<T> implements CompletionHandler<Integer, User> {

    private static Logger logger = Logger.getLogger(ReadWriteHandler.class);

    private MessageProcessor messageProcessor;
    private boolean read = true;

    ReadWriteHandler(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void completed(Integer bytesTreated, User user) {
        if (isReading()) {
            readInput(bytesTreated, user);

            if (user.hasItemsToWrite()) {
                logger.debug("Writing to the channel");

                writeToUserAndUnsetRead(user);
                return;
            }
        }

        readUserChannelAndSetRead(user);
    }

    private boolean isReading() {
        return read;
    }

    private void readInput(int bytesTreated, User user) {
        if (bytesTreated == -1) {
            closeChannel();
        } else if (bytesTreated == 0) {
            doNothing();
        } else {
            readMessage();
            messageProcessor.process(user);
        }
    }

    private void writeToUserAndUnsetRead(User user) {
        user.buffer.clear();
        ByteBuffer buffer = user.writeQueue.remove();
        user.buffer.put(buffer);
        user.buffer.flip();

        read = false;
        user.connection.write(user.buffer, user, this);
    }

    private void readUserChannelAndSetRead(User user) {
        user.buffer.clear();
        read = true;

        user.connection.read(user.buffer, user, this);
    }

    private void readMessage() {
        logger.debug("Reading the channel");
    }

    private void doNothing() {
        logger.debug("Channel is procrastinating");
    }

    private void closeChannel() {
        logger.debug("This channel read equals -1 and the channel should be closed");
    }

    @Override
    public void failed(Throwable e, User user) {
        logger.warn("Channel error", e);
    }
}
