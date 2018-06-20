package com.alphagfx.common.connection;

import com.alphagfx.common.Participant;

import java.nio.channels.CompletionHandler;

class WriteHandler implements CompletionHandler<Integer, Attachment> {

    // singleton because we are not going to do anything special with this
    private static final WriteHandler handler = new WriteHandler();

    private WriteHandler() {
    }

    static WriteHandler getHandler() {
        return handler;
    }

    @Override
    public void completed(Integer integer, Attachment attachment) {
        Participant user = Participant.NULL;
        if (user == null) {
            System.out.println("user is null");
        }
        if (user.getMessagesToSend().size() > 0) {
            user.writeMessage(user.getMessagesToSend().poll());
        }
    }

    @Override
    public void failed(Throwable throwable, Attachment attachment) {
        // TODO: 05/03/18 logger
        System.err.println("Failed to write to user");
        throwable.printStackTrace();
    }
}
