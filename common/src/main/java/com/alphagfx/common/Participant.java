package com.alphagfx.common;

import com.alphagfx.common.connection.Attachment;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Participant {
    public static final Participant NULL = new Participant(-1, "NO_USER");

    private final UserData data = new UserData();

    private Attachment attachment = null;

    private final Queue<Message> messagesToSend = new ConcurrentLinkedQueue<>();
    private final Queue<Message> messagesToReceive = new ConcurrentLinkedQueue<>();

    private Participant(int id, String name) {
        this.data.id = id;
        this.data.name = name;
    }

    public static Participant create(int id, String name) {
        return new Participant(id, name);
    }

    public boolean writeMessage(String message) {
        return writeMessage(new Message(-1, 0, message));
    }

    public boolean writeMessage(Message message) {
        ByteBuffer buffer = new StringCodec().encode(message).poll();
        boolean messageSent = attachment.write(buffer);
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
        return getData().id;
    }

    public String getName() {
        return getData().name;
    }

    public UserData getData() {
        return data;
    }

    public Queue<Message> getMessagesToReceive() {
        return messagesToReceive;
    }
    public Queue<Message> getMessagesToSend() {
        return messagesToSend;
    }


    @Override
    public String toString() {
        return "[id =" + getId() + ", name =" + getName() + "]";
    }
}

