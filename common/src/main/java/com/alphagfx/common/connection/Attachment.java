package com.alphagfx.common.connection;

import java.nio.ByteBuffer;

public interface Attachment {
    boolean read();

    boolean write(ByteBuffer message);
}
