package com.alphagfx.server;

public interface ProcessingQueue<T> {
    boolean add(T t);

    T poll();
}
