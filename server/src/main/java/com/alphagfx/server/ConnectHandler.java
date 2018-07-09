package com.alphagfx.server;

import org.apache.log4j.Logger;

import java.nio.channels.CompletionHandler;

class ConnectHandler implements CompletionHandler<Void, User> {

    private static Logger logger = Logger.getLogger(ConnectHandler.class);

    private MessageProcessor processor;

    ConnectHandler(MessageProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void completed(Void aVoid, User user) {
        CompletionHandler<Integer, User> rwHandler = processor.newConnection();
        user.connection.read(user.buffer, user, rwHandler);
    }

    @Override
    public void failed(Throwable e, User user) {
        logger.warn("Failed to connect destination", e);
    }
}
