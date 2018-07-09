package com.alphagfx.server;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

public class MainProcessor implements MessageProcessor, Factory<CompletionHandler> {
    @Override
    public CompletionHandler getAccept() {
        return new AcceptHandler(this);
    }

    @Override
    public CompletionHandler getRead() {
        return new ReadWriteHandler(this);
    }

    @Override
    public CompletionHandler getWrite() {
        return getRead();
    }

    @Override
    public CompletionHandler getConnect() {
        return new ConnectHandler(this);
    }

    @Override
    public void process(User user) {
        if (user == null) {
            return;
        }
        String message = getMessage(user);

        System.out.println("Client said: " + message);

        user.write(encode("You wrote " + message));
    }

    private String getMessage(User user) {
        ByteBuffer buffer = user.buffer;
        buffer.flip();

        String message = decode(buffer);

        buffer.clear();
        return message;
    }

    private String decode(ByteBuffer buffer) {
        return Charset.defaultCharset().decode(buffer).toString();
    }

    private ByteBuffer encode(String message) {
        return Charset.defaultCharset().encode(message);
    }

    @Override
    public CompletionHandler newConnection() {
        return getRead();
    }

}
