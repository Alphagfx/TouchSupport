package com.alphagfx.common;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.WritePendingException;
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

    public ByteBuffer buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);

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

    boolean online;
    private ConnectionHandlerAsync.WriteHandler writeHandler = ConnectionHandlerAsync.WriteHandler.getHandler();

    Participant(int id, String name) {
        this.id = id;
        this.name = name;
        countCreated++;
    }

    public Participant() {
        this.id = countCreated++;
        this.name = "Member #" + id;
    }

    public void writeMessage(Message message) {

        // encoding message
        Charset charset = Charset.forName(Const.CHARSET);
        byte[] bytes = message.getMessage().getBytes(charset);

        ByteBuffer buffer = ByteBuffer.allocate(Const.READ_BUFFER_SIZE);

        // putting command to execute for server, id of receiver and message itself
        buffer.clear();
        buffer.putInt(message.getCommand());
        buffer.putInt(id);
        buffer.put(bytes);
        buffer.flip();
        try {
            client.write(buffer, this, writeHandler);
        } catch (WritePendingException e) {
            messagesToSend.offer(message);
        }
    }
}
