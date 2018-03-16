package com.alphagfx.server;

import com.alphagfx.common.IProcessor;
import com.alphagfx.common.Message;
import com.alphagfx.common.Participant;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerProcessor implements IProcessor {

    // int should be id
    private final Map<Integer, Participant> users;
    private Map<Boolean, Participant> agents = new ConcurrentHashMap<>();

    private IProcessor specialProcessor;

    public ServerProcessor(Map<Integer, Participant> users, IProcessor processor) {
        this.users = users;
        specialProcessor = processor != null ? processor : (message, user) -> {

        };
    }

    @Override
    public void process(Message message, Participant user) {

        // for any additional actions
        specialProcessor.process(message, user);

        switch (message.getCommand()) {

            // just print the message to the console
            case -1: {
                System.out.println(message.getMessage());
                break;
            }

            // register user
            case 1: {
                String[] s = message.getMessage().split(" ");
                System.out.println(Arrays.toString(s));
                try {
                    user.setId(Integer.valueOf(s[0]));
                    user.setName(s[2]);
                    if (s[1].equals("agent")) {
                        agents.put(false, user);
                    }
                } catch (RuntimeException e) {
                    // TODO: 11/03/18 logger
                    System.err.println("Wrong data format");
                }
                users.put(user.getId(), user);
                break;
            }

            // message to the partner
            case 2: {
                Participant partner = users.get(message.getAddress());
                if (partner != null) {
                    partner.writeMessage(message);
                }
                break;
            }

            // get free agent
            case 3: {
                user.writeMessage(new Message(3, getFreeAgent().getId(), ""));
            }
        }
    }

    // should give you any free agent
    private Participant getFreeAgent() {
        return agents.get(false);
    }
}
