package com.alphagfx.common.connection;

import com.alphagfx.common.Codecs;
import com.alphagfx.common.Message;
import com.alphagfx.common.Participant;
import com.alphagfx.common.Processor;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

class ReadHandler implements CompletionHandler<Integer, Attachment> {

    private static Logger logger = Logger.getLogger(ReadHandler.class);

    private Processor processor;

    private ReadHandler(Processor processor) {
        this.processor = processor != null ? processor : (message, user) -> {

        };
    }

    public static ReadHandler create(Processor processor) {
        return new ReadHandler(processor);
    }

    @Override
    public void completed(Integer result, Attachment attachment) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Message message = Codecs.decode(buffer.getInt(), buffer);
        processor.process(message, Participant.NULL);
    }

    @Override
    public void failed(Throwable e, Attachment user) {
        logger.warn("Failed to read user", e);
    }
}
