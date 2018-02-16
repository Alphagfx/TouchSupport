import java.io.Console;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {


    public static void main(String[] args) {

        Console console = System.console();

        Scanner scanner = new Scanner(console.reader());

        while (true) {
            String line = console.readLine();
            System.out.println("You wrote this line: " + line);
            if (line.equals("exit")) {
                break;
            }
        }

    }

    private void connect() {
        try {
            Socket socket = new Socket();
            Scanner scanner = new Scanner(socket.getInputStream(), "UTF-8");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }

        } catch (IOException ignored) {
        }
    }
}
