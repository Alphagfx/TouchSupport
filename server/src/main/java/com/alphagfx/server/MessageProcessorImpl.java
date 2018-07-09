package com.alphagfx.server;

import com.alphagfx.common.database.UserDatabase;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

class MessageProcessorImpl implements MessageProcessor {
    private ProcessingQueue<User> queue;
    private UserDatabase userDB;

    public MessageProcessorImpl(ProcessingQueue<User> queue, UserDatabase userDB) {
        this.queue = queue;
        this.userDB = userDB;
    }

    //    @Override
    public void run() {
        while (true) {
            process(queue.poll());
        }
    }

    @Override
    public void process(User user) {
        if (user == null) {
            return;
        }
        ByteBuffer buffer = user.buffer;
        buffer.flip();
        String message = Charset.defaultCharset().decode(buffer).toString();
        System.out.println(message);
        user.write(Charset.defaultCharset().encode("You wrote " + message));
        buffer.clear();
    }

    // FIXME: 02/07/18 remove temp plug
    @Override
    public CompletionHandler newConnection() {
        return null;
    }
}
