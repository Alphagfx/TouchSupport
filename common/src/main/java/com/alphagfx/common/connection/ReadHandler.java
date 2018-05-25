package com.alphagfx.common.connection;

import com.alphagfx.common.IProcessor;
import com.alphagfx.common.Message;
import com.alphagfx.common.Participant;

import java.nio.channels.CompletionHandler;

class ReadHandler implements CompletionHandler<Integer, Attachment> {

    private IProcessor processor;

    private ReadHandler(IProcessor processor) {
        this.processor = processor != null ? processor : new IProcessor() {
            @Override
            public void process(Message message, Participant user) {

            }
        };
    }

    public static ReadHandler create(IProcessor processor) {
        return new ReadHandler(processor);
    }

    @Override
    public void completed(Integer result, Attachment attachment) {

        Message message = attachment.read(result);
        processor.process(message, attachment.getUser());
    }

    @Override
    public void failed(Throwable throwable, Attachment user) {
        // TODO: 05/03/18 logger
        System.err.println("Failed to read user");
        throwable.printStackTrace();
    }
}
