package com.alphagfx.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

class User {
    ByteBuffer buffer = ByteBuffer.allocate(2048);
    AsynchronousSocketChannel connection;

    User(AsynchronousSocketChannel connection) {
        this.connection = connection;
    }
}
