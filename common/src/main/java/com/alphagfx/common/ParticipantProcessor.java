package com.alphagfx.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ParticipantProcessor implements IParticipantProcessor {

    protected Map<Integer, Participant> users = new ConcurrentHashMap<>();

    @Override
    public void addParticipant(Participant p) {
        users.put(p.getId(), p);
    }

    @Override
    public void removeParticipant(Participant p) {
        removeParticipant(users.get(p).getId());
    }

    public void removeParticipant(int id) {
        users.remove(id);
    }
}
