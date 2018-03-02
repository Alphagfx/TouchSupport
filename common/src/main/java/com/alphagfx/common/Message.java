package com.alphagfx.common;

public class Message {

    private int command;
    private int size;
    private String message;

    public Message(int command, String message) {
        this.command = command;
        this.message = message;
        this.size = message.length() * Character.BYTES;
    }

    public int getCommand() {
        return command;
    }

    public int getSize() {
        return size;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Message) {
            Message m = ((Message) o);
            if (m.size == size && m.command == command && m.message.equals(message)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Command to execute: [" + command + "], size = " + size + ", message: " + message;
    }
}
