import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager implements Runnable {

    private static Logger logger = Logger.getLogger(ConnectionManager.class.getName());

    private Selector inputSelector;

    private List<ConnectionHandler> handlers = new LinkedList<>();

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private boolean running;

    private List<InetSocketAddress> pendingNewServers = new LinkedList<>();

    private Map<>

    public ConnectionManager() {
        try {
            inputSelector = Selector.open();
        } catch (IOException e) {
            logger.error("Error opening selector", e);
        }
    }

    public ConnectionManager(InetSocketAddress address) {
        super();
        addServer(address);
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
        } catch (IOException e) {
            logger.error("Error during server channel creation", e);
        }
    }

    private void addConnectionHandler() {
        ConnectionHandler handler = ConnectionHandler.getHandler();
        executor.submit(handler);
        handlers.add(handler);
    }

    @Override
    public void run() {

        while (running) {

//            remove dead handlers
            handlers.stream().filter(h -> !h.isListening()).forEach(h -> handlers.remove(h));

//            add new server channels
            if (pendingNewServers.size() != 0) {
                for (InetSocketAddress address : pendingNewServers) {
                    addServer(address);
                }
                pendingNewServers.clear();
            }

            try {
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
        try {
            channel.configureBlocking(false);
        } catch (IOException e) {
            logger.warn("Something happened with new channel", e);
        }
        channel.register(inputSelector, SelectionKey.OP_READ);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
