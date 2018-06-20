package com.alphagfx.common.connection;

import com.alphagfx.common.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.channels.AsynchronousSocketChannel;

public class AttachmentTest {

    private Attachment attach;

    @Before
    public void create() {
        attach = null;
    }

    @Test
    public void read() {

        Mockito.mock(AsynchronousSocketChannel.class);

        attach.read();
    }


    @Test
    public void write() {
        Message message = new Message(0, 0, "test");
    }
}
