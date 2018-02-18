import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionHandler implements Runnable {

    private Socket socket;

    private boolean listening = true;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while (isListening()) {
                try {
                    Message message = ((Message) in.readObject());
                    System.out.println(message.getMessage());
                } catch (EOFException messageFail) {
                    System.err.println(messageFail);
                }
            }

        } catch (SocketException e) {
            System.out.println("0");
            System.err.println(e);
        } catch (IOException e) {
            System.out.println("1");
            System.err.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println("2");
            System.err.println(e);
        }

    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public void sendMessage(Message message) {

    }
}
