package com.alphagfx.common;

/**
 * Interface for processing incoming messages
 */
public interface Processor {

    void process(Message message, Participant user);
}
