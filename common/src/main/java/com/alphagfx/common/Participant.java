package com.alphagfx.common;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Participant {

    private static int countCreated = 0;

    private final Queue<Message> messagesToSend = new ConcurrentLinkedQueue<>();
    private final Queue<Message> messagesToReceive = new ConcurrentLinkedQueue<>();
    private String name;
    private SocketAddress address;
    private int id;
    public AsynchronousSocketChannel client;
    AsynchronousServerSocketChannel server;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public Queue<Message> getMessagesToReceive() {
        return messagesToReceive;
    }

    public Queue<Message> getMessagesToSend() {
        return messagesToSend;
    }

    @Override
    public String toString() {
        return "[ id = " + id + " , name = " + name + " ]";
    }

    ByteBuffer buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);
    private WriteHandler writeHandler = new WriteHandler();

    Participant(int id, String name) {
        this.id = id;
        this.name = name;
        countCreated++;
    }

    public Participant() {
        this.id = countCreated++;
        this.name = "Member #" + id;
    }

    public void readMessage() {

    }

    public void writeMessage(String message) {

        // encoding message
        Charset charset = Charset.forName(Const.CHARSET);
        byte[] bytes = message.getBytes(charset);

        buffer.clear();
        buffer.put(bytes);
        buffer.flip();
        client.write(buffer, this, writeHandler);
    }
}
