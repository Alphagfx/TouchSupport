package com.alphagfx.common;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StringCodec implements Codec {

    private int codecId = 1;

    @Override
    public Queue<ByteBuffer> encode(Message message) {
        Queue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();

        ByteBuffer buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);
        buffer.putInt(codecId);
        buffer.put(message.getMessage().getBytes());

        queue.add(buffer);
        return queue;
    }

    @Override
    public Message decode(ByteBuffer buffer) {
        String string = Charset.defaultCharset().decode(buffer).toString();
        Message message = new Message(-1, -1, string);
        return message;
    }
}
