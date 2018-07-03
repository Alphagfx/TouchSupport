package com.alphagfx.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

class User {
    ByteBuffer buffer = ByteBuffer.allocate(2048);
    AsynchronousSocketChannel connection;

    Queue<ByteBuffer> writeQueue = new LinkedBlockingQueue<>();

    User(AsynchronousSocketChannel connection) {
        this.connection = connection;
    }

    public void write(ByteBuffer message) {
        writeQueue.add(message);
    }

    boolean hasItemsToWrite() {
        return writeQueue.size() != 0;
    }
}
