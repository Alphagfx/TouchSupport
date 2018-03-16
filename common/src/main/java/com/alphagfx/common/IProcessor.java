package com.alphagfx.common;

/**
 * Interface for processing incoming messages
 */
public interface IProcessor {

    void process(Message message, Participant user);
}
