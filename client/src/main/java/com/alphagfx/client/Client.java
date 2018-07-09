package com.alphagfx.client;

import com.alphagfx.common.Const;
import com.alphagfx.common.Message;
import com.alphagfx.common.Participant;
import com.alphagfx.common.connection.Attachment;
import com.alphagfx.common.connection.AttachmentImpl;
import com.alphagfx.common.connection.ConnectionHandlerAsync;
import com.alphagfx.common.database.MapDatabase;
import com.alphagfx.common.database.UserDatabase;
import org.apache.commons.lang3.EnumUtils;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Client implements Runnable {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Logger logger = Logger.getLogger(Client.class.getName());

    private int id = new Random().nextInt();

    public static void main(String[] args) {

        executorService.execute(new Client());

    }

    private static void exit() {
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
        Participant user = Participant.create(-3, "new_client");

        UserDatabase users = MapDatabase.create();

        users.put(-3, user);

        InetSocketAddress address = new InetSocketAddress("localhost", Const.CLIENT_PORT);
        UserDatabase userDB = MapDatabase.create();
        ConnectionHandlerAsync handler = ConnectionHandlerAsync.create(address, users, userDB, (message, user1) -> System.out.println("" + user1 + message));

//        executorService.execute(handler);

        SocketAddress serverAddr = new InetSocketAddress("localhost", Const.SERVER_PORT);

//        Attachment attachment = new AttachmentImpl.Builder().setUser(user).setClient(handler.connect(serverAddr)).build();
        Attachment attachment = AttachmentImpl.create(handler.connect(serverAddr), null, null);

//        user.getAttachment().client = handler.connect(serverAddr);

        Scanner input = new Scanner(System.in);
        String line;

        boolean active = true;
        while (active) {

            line = input.nextLine();

            String[] strings = line.split("\\W");
            System.out.println(Arrays.toString(strings));

            String[] strings1 = line.split(" ");
            System.out.println(Arrays.toString(strings1));


            String c = Arrays.stream(line.split("\\W")).filter(s -> ("/" + EnumUtils.getEnum(Commands.class, s)).equals("/" + s)).findFirst().orElse(Commands.DEFAULT.name());
            System.out.println(c);
            System.out.println(c.length());
            String text = line.substring(Objects.equals(c, Commands.DEFAULT.name()) ? 0 : c.length() + 2);

            Commands command = Commands.valueOf(c);

            switch (command) {
                // TODO: 17/03/18 parse config file for servers
                case connect: {

                    break;
                }
                case register: {
                    user.writeMessage(new Message(1, 0, "" + id + ":" + "" + text));
                    break;
                }
                case login: {
                    user.writeMessage(new Message(2, 0, text));
                    break;
                }
                case exit: {
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
        connect,
        register,
        login,
        exit,
        DEFAULT
    }
}