import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Chat {

    private final Map<Integer, Participant> participants = Collections.synchronizedMap(new HashMap<Integer, Participant>());

    public Chat() {

    }

    public Participant addParticipant(int id, SocketAddress address) {
        Participant participant = new Participant(id, "member " + id);
        participant.setAddress(address);
        participants.put(id, participant);
        return participant;
    }

    public Participant removeParticipant(int id) {
        return participants.remove(id);
    }

    public void sendMessageTo(int id, Message message) {
        Participant participant = participants.get(id);
        if (participant != null) {
            participant.messagesToSend.add(message);
        }
    }

    public void sendEveryone(Message message) {
        participants.forEach((integer, participant) -> participant.messagesToSend.add(message));
    }

    public LinkedList<Message> readEveryone() {
        System.out.println("enter read everyone");
        LinkedList<Message> messages = new LinkedList<>();
        participants.forEach((integer, participant) -> messages.addAll(participant.messagesToReceive));
        return messages;
    }

    public LinkedList<String> checkParticipants() {
        LinkedList<String> list = new LinkedList<>();
        participants.forEach(((integer, participant) -> list.add(participant.toString())));
        return list;
    }

    public class Participant {

        private final Queue<Message> messagesToReceive = new ConcurrentLinkedQueue<>();
        private String name;
        private SocketAddress address;
        private final Queue<Message> messagesToSend = new ConcurrentLinkedQueue<>();
        private int id;

        private Participant(int id, String name) {
            this.id = id;
            this.name = name;
        }


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public SocketAddress getAddress() {
            return address;
        }

        public void setAddress(SocketAddress address) {
            this.address = address;
        }

        public Queue<Message> getMessagesToReceive() {
            return messagesToReceive;
        }

        public Queue<Message> getMessagesToSend() {
            return messagesToSend;
        }

        @Override
        public String toString() {
            return "[ id = " + id + " , name = " + name + " ]";
        }
    }

}
