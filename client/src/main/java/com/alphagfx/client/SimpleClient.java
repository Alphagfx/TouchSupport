package com.alphagfx.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class SimpleClient {

    private AsynchronousSocketChannel connection = AsynchronousSocketChannel.open();

    public SimpleClient() throws IOException {
    }

    public static void main(String[] args) throws IOException {
        new SimpleClient().readInput();
    }

    private void readInput() {
        Scanner scanner = new Scanner(System.in);

        String line = "";
        while (!line.equalsIgnoreCase("exit")) {
            System.out.println("message : " + line);
            line = scanner.nextLine();

            if (line.equalsIgnoreCase("connect")) {
                connect();
            }

            try {
                connection.write(Charset.defaultCharset().encode(line));
            } catch (NotYetConnectedException e) {
                System.out.println("Not yet connected");
            }
        }
    }

    private void connect() {
        connection.connect(new InetSocketAddress(5000));
    }

}
