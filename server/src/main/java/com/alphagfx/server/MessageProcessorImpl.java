package com.alphagfx.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class MessageProcessorImpl implements MessageProcessor {
    private ProcessingQueue<User> queue;

    public MessageProcessorImpl(ProcessingQueue<User> queue) {
        this.queue = queue;
    }

    @Override
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
        System.out.println(Charset.defaultCharset().decode(buffer));
        buffer.clear();
    }
}
