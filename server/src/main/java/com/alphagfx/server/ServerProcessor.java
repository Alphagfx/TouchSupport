package com.alphagfx.server;

import com.alphagfx.common.Message;
import com.alphagfx.common.Participant;
import com.alphagfx.common.Processor;
import com.alphagfx.common.database.UserDatabase;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerProcessor implements Processor {

    private UserDatabase<Participant> registeredUsers;
    private UserDatabase<Participant> users;

    private Map<Boolean, Participant> agents = new ConcurrentHashMap<>();

    private Processor specialProcessor;

    private int id = 0;

    public ServerProcessor(UserDatabase users, UserDatabase registeredUsers, Processor processor) {
        this.users = users;
        this.registeredUsers = registeredUsers;
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

            // register user (id + role + name + password?)
            case 1: {
                String[] s = message.getMessage().split(":");
                System.out.println(Arrays.toString(s));
                try {
                    /*
                    user.setId(Integer.valueOf(s[0]));
                    if (s[1].equals("agent")) {
                        agents.put(false, user);
                    }
                    user.setName(s[2]);
                    user.setPassword(s[3]);
                    */
                } catch (RuntimeException e) {
                    // TODO: 11/03/18 logger
                    System.err.println("Wrong data format");
                }
                registeredUsers.put(id++, user);
                break;
            }

            //login user (id + password)
            case 2: {
                String[] s = message.getMessage().split(" ");

                if (s.length != 2) {
                    user.writeMessage("Wrong data format");
                    return;
                }

                int id;
                try {
                    id = Integer.valueOf(s[0]);
                } catch (NumberFormatException e) {
                    user.writeMessage("Wrong id");
                    return;
                }

                Participant registeredUser = registeredUsers.get(id);
//                if (registeredUser != null && registeredUser.password.equals(s[1])) {
//                    user.getAttachment().client = registeredUser.getAttachment().client;
//                    users.remove(user.getId());
//                }
                break;
            }

            // message to the partner
            case 3: {
                Participant partner = users.get(message.getAddress());
                if (partner != null) {
                    partner.writeMessage(message);
                }
                break;
            }

            // get free agent
            case 4: {
                user.writeMessage(new Message(3, getFreeAgent().getId(), ""));
            }
        }
    }

    // should give you any free agent
    private Participant getFreeAgent() {
        return agents.get(false);
    }
}
