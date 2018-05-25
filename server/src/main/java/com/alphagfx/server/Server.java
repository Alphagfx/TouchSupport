package com.alphagfx.server;

import com.alphagfx.common.Const;
import com.alphagfx.common.IProcessor;
import com.alphagfx.common.Message;
import com.alphagfx.common.Participant;
import com.alphagfx.common.connection.ConnectionHandlerAsync;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static Logger logger = Logger.getLogger(Server.class.getName());

    private ExecutorService executor;

    public Server(ExecutorService executor) {
        this.executor = executor;
    }

    public static void main(String[] args) {
        new Server(Executors.newSingleThreadExecutor()).run();
    }

    @Override
    public void run() {

        ConcurrentMap<Integer, Participant> users = new ConcurrentHashMap<>();
        Map<Integer, Participant> registeredUsers = new ConcurrentHashMap<>();

        InetSocketAddress address = new InetSocketAddress("localhost", Const.SERVER_PORT);

        ServerProcessor processor = new ServerProcessor(users, registeredUsers, new IProcessor() {
            @Override
            public void process(Message message, Participant user) {
                System.out.println("" + user + " | " + message);
            }
        });

        ConnectionHandlerAsync handler = new ConnectionHandlerAsync(address, users, processor);

        handler.launchServer();

        executor.execute(handler);

        // TODO: 11/03/18 Replace with smth more appropriate to this situation
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
