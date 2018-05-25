package com.alphagfx.common.connection;

import com.alphagfx.common.Message;
import org.junit.Before;
import org.junit.Test;

public class AttachmentTest {

    private Attachment attach;

    @Before
    public void create() {
        attach = new Attachment.Builder().build();
    }

    @Test
    public void read() {
        attach.readChannelWithHandler();
    }

    @Test
    public void write() {
        Message message = new Message(0, 0, "test");
        attach.writeMessage(message);

    }
}
