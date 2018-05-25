package com.alphagfx.common;

import com.alphagfx.common.connection.Attachment;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Participant {

    private static int countCreated = 0;

    private final Queue<Message> messagesToSend = new ConcurrentLinkedQueue<>();
    private final Queue<Message> messagesToReceive = new ConcurrentLinkedQueue<>();

    private final String name;
    private final int id;
    // TODO: 23/03/18 security knows my name
    public String password;
    private Attachment attachment = null;

    private Participant(int id, String name) {
        this.id = id;
        this.name = name;
        countCreated++;
    }

    private Participant() {
        this.id = countCreated++;
        this.name = "Member #" + id;
    }

    public static Participant create(int id, String name) {
        return new Participant(id, name);
    }

    public boolean writeMessage(String message) {
        return writeMessage(new Message(-1, 0, message));
    }

    public boolean writeMessage(Message message) {
        boolean messageSent = attachment.writeMessage(message);
        if (!messageSent) {
            messagesToSend.offer(message);
        }
        return messageSent;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Queue<Message> getMessagesToReceive() {
        return messagesToReceive;
    }
    public Queue<Message> getMessagesToSend() {
        return messagesToSend;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "[ id = " + id + " , name = " + name + " ]";
    }
}

