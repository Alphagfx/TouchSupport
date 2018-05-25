package com.alphagfx.common.connection;

import com.alphagfx.common.Const;
import com.alphagfx.common.Message;
import com.alphagfx.common.Participant;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.WritePendingException;
import java.nio.charset.Charset;
import java.util.Objects;

public class Attachment {

    private static CompletionHandler<Integer, Attachment> writeHandler = WriteHandler.getHandler();
    private ByteBuffer buffer;
    private Participant user;
    private AsynchronousSocketChannel client;
    private AsynchronousServerSocketChannel server;
    private CompletionHandler read;
    private CompletionHandler write;

    private Attachment(Builder builder) {
        this.user = builder.user;
        this.client = builder.client;
        this.server = builder.server;
        this.read = builder.read;
        this.write = builder.write;
        this.buffer = builder.buffer;
    }

    Message read(int result) {
        if (result == -1) {
            closeChannel();
            return null;
        }

        Message message = decodeMessageFromBuffer();

        getUser().getMessagesToReceive().offer(message);

//        System.out.println("Other side says: " + message);

        // relaunching listener automatically
        readChannelWithHandler();

        return message;
    }

    public boolean writeMessage(Message message) {

        ByteBuffer writeBuffer = encodeMessageIntoBuffer(message);
        try {
            client.write(writeBuffer, this, writeHandler);
            return true;
        } catch (WritePendingException e) {
            return false;
        }
    }

    void readChannelWithHandler() {
        client.read(buffer, this, read);
    }

    private void closeChannel() {
        // TODO: 24/05/18 logger
        try {
            client.close();
            System.err.println("Stopped listening client: " + getUser().getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Message decodeMessageFromBuffer() {
        buffer.flip();
        int command = buffer.getInt();
        int address = buffer.getInt();

        byte[] bytes = new byte[buffer.limit() - Integer.BYTES * 2];

        buffer.get(bytes);
        buffer.clear();

        Charset charset = Charset.forName(Const.CHARSET);

        return new Message(command, address, new String(bytes, charset));
    }

    private ByteBuffer encodeMessageIntoBuffer(Message message) {
        Charset charset = Charset.forName(Const.CHARSET);
        byte[] bytes = message.getMessage().getBytes(charset);

        ByteBuffer writeBuffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);

        // putting command to execute for server, id of receiver and message itself
        writeBuffer.clear();
        writeBuffer.putInt(message.getCommand()).putInt(message.getAddress()).put(bytes).flip();

        return buffer;
    }

    Participant getUser() {
        return user;
    }

    void setUser(Participant user) {
        this.user = user;
    }

    public static class Builder {
        private Participant user = null;

        private AsynchronousSocketChannel client;
        private AsynchronousServerSocketChannel server;

        private CompletionHandler<Integer, Attachment> read;
        private CompletionHandler<Integer, Attachment> write;

        private ByteBuffer buffer;

        public Builder() {

        }

        public Attachment build() {
            if (Objects.isNull(buffer)) {
                buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);
            }

            if (Objects.isNull(read)) {
                // FIXME: 04/05/18
                read = ReadHandler.create(null);
            }

            Attachment result = new Attachment(this);

            if (Objects.nonNull(user)) {
                user.setAttachment(result);
            }

            return result;
        }

        public Builder setUser(Participant user) {
            this.user = user;
            return this;
        }

        public Builder setClient(AsynchronousSocketChannel client) {
            this.client = client;
            return this;
        }

        public Builder setServer(AsynchronousServerSocketChannel server) {
            this.server = server;
            return this;
        }

        public Builder setRead(CompletionHandler read) {
            this.read = read;
            return this;
        }

        public Builder setWrite(CompletionHandler write) {
            this.write = write;
            return this;
        }

        public Builder setBuffer(ByteBuffer buffer) {
            this.buffer = Objects.requireNonNull(buffer);
            return this;
        }
    }
}
