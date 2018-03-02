package com.alphagfx.common;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager implements Runnable {

    private static Logger logger = Logger.getLogger(ConnectionManager.class.getName());

    private Selector inputSelector;

    private List<ConnectionHandler> handlers = new LinkedList<>();

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private boolean running = true;

    private List<InetSocketAddress> pendingNewServers = Collections.synchronizedList(new LinkedList<>());
    private List<InetSocketAddress> pendingNewConnection = Collections.synchronizedList(new LinkedList<>());

    private Map<Integer, Participant> users = new ConcurrentHashMap<>();

    private IParticipantProcessor processor;

    private int u = 0;

    public ConnectionManager(IParticipantProcessor processor) {
        this.processor = processor;
        try {
            inputSelector = Selector.open();
        } catch (IOException e) {
            logger.error("Error opening selector", e);
        }
    }

    public ConnectionManager(IParticipantProcessor processor, InetSocketAddress address) {
        this(processor);
        addServer(address);
        System.out.println("Created server");
    }

    public Map<Integer, Participant> getUsers() {
        return users;
    }


    /**
     * Literally adds @{@link ServerSocketChannel} with given address
     *
     * @param address
     */
    public void addInputConnectionListener(InetSocketAddress address) {
        pendingNewServers.add(address);
    }

    /**
     * Literally adds @{@link ServerSocketChannel} with given address
     */
    public void addInputConnectionListener(InetAddress address, int port) {
        pendingNewServers.add(new InetSocketAddress(address, port));
    }

    /**
     * Literally adds @{@link ServerSocketChannel} with given address
     *
     * @param address
     */
    private void addServer(InetSocketAddress address) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(address);
            serverSocketChannel.register(inputSelector, SelectionKey.OP_ACCEPT);
            System.out.println("Server added");
        } catch (IOException e) {
            logger.error("Error during server channel creation", e);
        }
    }

    public void addConnectionHandler() {
        ConnectionHandler handler = ConnectionHandler.getHandler(this, processor);
        executor.submit(handler);
        handlers.add(handler);
        System.out.println("Handler added");
    }

    public void openConnection(InetSocketAddress address) {
        System.out.println("Connection-pending added");
        pendingNewConnection.add(address);
    }

    @Override
    public void run() {

        System.out.println("Launched manager");

        while (running) {

            System.out.println("Before remove dead handlers");

//            remove dead handlers
            handlers.stream().filter(h -> !h.isListening()).forEach(h -> handlers.remove(h));

            System.out.println("After handler clear");

//            add new server channels
            if (pendingNewServers.size() != 0) {
                for (InetSocketAddress address : pendingNewServers) {
                    addServer(address);
                    System.out.println("Server added");
                }
                pendingNewServers.clear();
                System.out.println("After adding new servers");
            }


            if (pendingNewConnection.size() != 0) {
                for (InetSocketAddress address : pendingNewConnection) {
                    System.out.println("Adding new connection " + address.toString());
                    try {
                        SocketChannel channel = SocketChannel.open(address);
                        registerNewChannel(channel);
                    } catch (IOException e) {
                        logger.warn("Connection problem", e);
                        System.out.println("Connection problem");
                    }
                    System.out.println("Connection added");
                }
                pendingNewConnection.clear();
                System.out.println("After adding new connections");
            }


            try {
                System.out.println("Before select: " + Arrays.toString(inputSelector.keys().toArray()));
                inputSelector.select();
            } catch (IOException e) {
                logger.error("Selector select error", e);
            }

            Set<SelectionKey> selectionKeys = inputSelector.selectedKeys();

            for (SelectionKey key : selectionKeys) {
                if (key.isValid() && key.isAcceptable()) {
                    accept(key);
                }
            }
        }

    }

    private void accept(SelectionKey key) {
        try {
            System.out.println("Accepting channel");
            Object attachment = key.attachment();
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

            SocketChannel channel = serverSocketChannel.accept();

            registerNewChannel(channel);

            serverSocketChannel.register(inputSelector, SelectionKey.OP_ACCEPT, attachment);
        } catch (ClosedChannelException e) {
            logger.warn("Channel closed", e);
        } catch (IOException e) {
            logger.error("Server: channel accept exception", e);
        }
    }

    private void registerNewChannel(SocketChannel channel) throws ClosedChannelException {
        System.out.println("Registering new user");
        try {
            channel.configureBlocking(false);
        } catch (IOException e) {
            logger.warn("Something happened with new channel", e);
        }
        Participant participant = new Participant(-1, "");
        handlers.get(0).addChannel(participant, channel);
        users.put(u++, participant);
        System.out.println("Map: " + users.toString());
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
