package com.alphagfx.common;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Chat {

    private final Map<Integer, Participant> participants = Collections.synchronizedMap(new HashMap<Integer, Participant>());

    public Chat() {

    }

    public void addParticipant(Participant participant) {
        participants.put(participant.getId(), participant);
    }

    private Participant addParticipant(int id, SocketAddress address) {
//        Participant participant = new Participant(id, "member " + id);
        Participant participant = Participant.create(id, "member " + id);
        participants.put(id, participant);
        return participant;
    }

    public Participant removeParticipant(int id) {
        return participants.remove(id);
    }

    public void sendMessageTo(int id, Message message) {
        if (message == null) {
            return;
        }
        Participant participant = participants.get(id);
        System.out.println("Sending message to " + participant.getId());

        if (participant != null) {
            participant.getMessagesToSend().add(message);
        }
    }

    public void sendEveryone(Message message) {
        if (message == null) {
            return;
        }
        participants.forEach((integer, participant) -> {
            System.out.println("Sending message to " + participant.getId());
            participant.getMessagesToSend().add(message);
        });
    }

    public LinkedList<Message> readEveryone() {
//        System.out.println("enter read everyone");
        LinkedList<Message> messages = new LinkedList<>();
        participants.forEach((integer, participant) -> messages.addAll(participant.getMessagesToReceive()));
        return messages;
    }

    public LinkedList<String> checkParticipants() {
        LinkedList<String> list = new LinkedList<>();
        participants.forEach(((integer, participant) -> list.add(participant.toString())));
        return list;
    }


}
