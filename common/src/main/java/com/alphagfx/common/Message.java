package com.alphagfx.common;

public class Message {

    private int command;
    private int address;
    private int size;
    private String message;

    public Message(int command, int address, String message) {
        this.command = command;
        this.address = address;
        this.message = message;
        this.size = message.length() * Character.BYTES;
    }

    public int getCommand() {
        return command;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
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
            return m.size == size && m.command == command && m.address == address && m.message.equals(message);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Command to execute: [" + command + "], size = " + size + ", address = " + address + ", message: " + message;
    }
}
