package com.alphagfx.server;

public interface MessageProcessor extends Runnable {
    void process(User user);
}
