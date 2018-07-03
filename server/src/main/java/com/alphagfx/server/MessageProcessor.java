package com.alphagfx.server;

import java.nio.channels.CompletionHandler;

public interface MessageProcessor {
    void process(User user);

    CompletionHandler newConnection();
}
