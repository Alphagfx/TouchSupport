package com.alphagfx.common.connection;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;

public class AttachmentImpl implements Attachment {

    private static final Logger logger = Logger.getLogger(AttachmentImpl.class);

    private AsynchronousSocketChannel connection;
    private CompletionHandler<Integer, Attachment> read;
    private CompletionHandler<Integer, Attachment> write;

    private AttachmentImpl(AsynchronousSocketChannel connection, CompletionHandler<Integer, Attachment> read,
                           CompletionHandler<Integer, Attachment> write) {
        this.connection = connection;
        this.read = read;
        this.write = write;
    }

    public static AttachmentImpl create(AsynchronousSocketChannel connection, CompletionHandler<Integer, Attachment> read,
                                        CompletionHandler<Integer, Attachment> write) {
        return new AttachmentImpl(connection, read, write);
    }

    @Override
    public boolean read() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            connection.read(buffer, this, read);
            return true;
        } catch (ReadPendingException e) {
            return false;
        }
    }

    @Override
    public boolean write(ByteBuffer buffer) {
        try {
            connection.write(buffer, this, write);
            return true;
        } catch (WritePendingException e) {
            return false;
        }
    }

    private void closeChannel() {
        try {
            connection.close();
            logger.info("Stopped listening connection");
        } catch (IOException e) {
            logger.warn("Error while closing connection channel", e);
        }
    }

}
