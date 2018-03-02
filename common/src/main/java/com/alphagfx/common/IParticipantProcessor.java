package com.alphagfx.common;

public interface IParticipantProcessor {

    void process();

    void addParticipant(Participant p);

    void removeParticipant(Participant p);
}
