import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class Chat {

    private HashMap<String, Participant> participants;

    public class Participant {

        private String name;
        private SocketAddress address;
        private LinkedList<Message> messagesToReceive;
        private LinkedList<Message> messagesToSend;

        private Participant() {
            messagesToReceive = ((LinkedList<Message>) Collections.synchronizedList(new LinkedList<Message>()));
            messagesToSend = ((LinkedList<Message>) Collections.synchronizedList(new LinkedList<Message>()));
        }
    }
}
