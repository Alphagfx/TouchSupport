package com.alphagfx.common;

import java.nio.ByteBuffer;
import java.util.Queue;

public interface Codec {

    Queue<ByteBuffer> encode(Message message);

    Message decode(ByteBuffer buffer);
}
