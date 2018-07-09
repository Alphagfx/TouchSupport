package com.alphagfx.common;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class Codecs {
    private static Codec NULL = new Codec() {
        @Override
        public Queue<ByteBuffer> encode(Message message) {
            return new LinkedList<>();
        }

        @Override
        public Message decode(ByteBuffer buffer) {
            return new Message(-1, -1, "NULL");
        }
    };

    private static Map<Integer, Codec> codecs = new ConcurrentHashMap<>();

    public static Message decode(int codec, ByteBuffer buffer) {
        return getCodec(codec).decode(buffer);
    }

    public static Queue<ByteBuffer> encode(int codec, Message message) {
        return getCodec(codec).encode(message);
    }

    private static Codec getCodec(int codec) {
        return codecs.getOrDefault(codec, NULL);
    }

}
