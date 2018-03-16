package com.alphagfx.client;

import com.alphagfx.common.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;


public class Client implements Runnable {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Logger logger = Logger.getLogger(Client.class.getName());

    private final int id = RandomUtils.nextInt(1, Integer.MAX_VALUE);

    public static void main(String[] args) {

        executorService.execute(new Client());

    }

    public static void exit() {
        try {
            System.out.println("attempt to shutdown executor");
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info("task interrupted", e);
        } finally {
            if (!executorService.isTerminated()) {
                logger.warn("cancel non-finished tasks");
            }
            executorService.shutdownNow();
            logger.info("shutdown finished");
        }
    }

    @Override
    public void run() {
        runAnother();
    }

    // TODO: 07/03/18 replace run()
    private void runAnother() {
        Participant user = new Participant();

        ConcurrentMap<Integer, Participant> users = new ConcurrentHashMap<>();

        users.put(user.getId(), user);

        InetSocketAddress address = new InetSocketAddress("localhost", Const.CLIENT_PORT);
        ConnectionHandlerAsync handler = new ConnectionHandlerAsync(address, users, new IProcessor() {
            @Override
            public void process(Message message, Participant user) {
                System.out.println("" + user + message);
            }
        });
        executorService.execute(handler);

        SocketAddress serverAddr = new InetSocketAddress("localhost", Const.SERVER_PORT);

        user.client = handler.connect(serverAddr);

        Scanner input = new Scanner(System.in);
        String line;

        boolean active = true;
        while (active) {

            line = input.nextLine();

            String c = Arrays.stream(line.split("\\W")).filter(s -> EnumUtils.getEnum(Commands.class, s) != null).findFirst().orElse(Commands.DEFAULT.name());

            System.out.println(c);
            Commands command = Commands.valueOf(c);

            switch (command) {
                case register: {
                    user.writeMessage(new Message(1, 0, "" + id + " " + line.substring(9)));
                    break;
                }
                case LOGIN: {

                    break;
                }
                case EXIT: {
                    active = false;
                    break;
                }
                case DEFAULT: {
                    user.writeMessage(new Message(-1, 0, line));
                }
            }
        }

        handler.exit();
        exit();

    }

    enum Commands {
        register("/register"),
        LOGIN("/login"),
        EXIT("/exit"),
        DEFAULT("");

        private String string;

        Commands(String s) {
            string = s;
        }

        public String value() {
            return string;
        }
    }
}