package com.alphagfx.server;

import com.alphagfx.common.*;
import com.alphagfx.common.connection.ConnectionHandlerAsync;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static Logger logger = Logger.getLogger(Server.class);

    private ExecutorService executor;

    public Server(ExecutorService executor) {
        this.executor = executor;
    }

    public static void main(String[] args) {
        new Server(Executors.newSingleThreadExecutor()).run();
    }

    @Override
    public void run() {

        UserDatabase users = MapDatabase.create();
        UserDatabase registeredUsers = MapDatabase.create();

        InetSocketAddress address = new InetSocketAddress("localhost", Const.SERVER_PORT);

        Processor processor = new ServerProcessor(users, registeredUsers, new Processor() {
            @Override
            public void process(Message message, Participant user) {
                System.out.println("" + user + " | " + message);
            }
        });

        ConnectionHandlerAsync handler = ConnectionHandlerAsync.create(address, users, registeredUsers, processor);

        handler.launchServer();

        // TODO: 11/03/18 Replace with smth more appropriate to this situation
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
