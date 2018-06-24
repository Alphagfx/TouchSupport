package com.alphagfx.server;

import java.nio.channels.CompletionHandler;

public interface Factory<T extends CompletionHandler> {
    T getAccept();

    T getRead();

    T getWrite();
}
