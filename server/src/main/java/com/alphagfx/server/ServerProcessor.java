package com.alphagfx.server;

import com.alphagfx.common.Message;
import com.alphagfx.common.ParticipantProcessor;

public class ServerProcessor extends ParticipantProcessor {

    @Override
    public void process() {
        users.forEach((id, p) -> {
            for (Message m : p.getMessagesToReceive()) {
                processMessage(m);
            }
            p.getMessagesToReceive().clear();
        });
        users.clear();
    }

    private void processMessage(Message message) {
        System.out.println(message);
    }
}
