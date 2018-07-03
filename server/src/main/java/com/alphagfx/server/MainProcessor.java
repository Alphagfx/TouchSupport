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
        return new ReadWriteHandler(this);
    }

    @Override
    public CompletionHandler getConnect() {
        return null;
    }

    @Override
    public void process(User user) {
        if (user == null) {
            return;
        }
        ByteBuffer buffer = user.buffer;
        buffer.flip();
        String message = Charset.defaultCharset().decode(buffer).toString();
        buffer.clear();
        System.out.println("Client said: " + message);
        user.write(Charset.defaultCharset().encode("You wrote " + message));
    }

    @Override
    public CompletionHandler newConnection() {
        return getRead();
    }

}
