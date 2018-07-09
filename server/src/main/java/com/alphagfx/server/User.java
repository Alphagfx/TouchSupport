package com.alphagfx.server;

import com.alphagfx.common.Updatable;
import com.alphagfx.common.UserData;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

class User implements Updatable<User> {
    ByteBuffer buffer = ByteBuffer.allocate(2048);
    AsynchronousSocketChannel connection;

    private UserData userData;

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

    @Override
    public void update(User toUpdateFrom) {
        userData.update(toUpdateFrom.userData);
    }
}
