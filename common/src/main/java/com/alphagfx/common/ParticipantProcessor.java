package com.alphagfx.common;

import java.util.Map;

public abstract class ParticipantProcessor implements IParticipantProcessor {

    protected Map<Integer, Participant> users;

    protected ParticipantProcessor(Map<Integer, Participant> users) {
        this.users = users;
    }

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
