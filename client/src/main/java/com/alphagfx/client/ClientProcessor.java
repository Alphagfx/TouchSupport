package com.alphagfx.client;

import com.alphagfx.common.Message;
import com.alphagfx.common.ParticipantProcessor;

public class ClientProcessor extends ParticipantProcessor {

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
        System.out.println("and actually say hello");
    }
}

