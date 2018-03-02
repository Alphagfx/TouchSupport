package com.alphagfx.common;

import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Participant {

    private final Queue<Message> messagesToSend = new ConcurrentLinkedQueue<>();
    private final Queue<Message> messagesToReceive = new ConcurrentLinkedQueue<>();
    private String name;
    private SocketAddress address;
    private int id;

    Participant(int id, String name) {
        this.id = id;
        this.name = name;
    }


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
}
