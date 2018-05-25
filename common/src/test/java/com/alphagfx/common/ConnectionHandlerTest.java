package com.alphagfx.common;

import com.alphagfx.common.connection.ConnectionHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

public class ConnectionHandlerTest {

    private IUserDB db;
    private ConnectionHandler handler;

    private String addr = "localhost";
    private int port = Const.SERVER_PORT;

    @Test
    public void create() {
        IUserDB db = new IUserDB() {
            @Override
            public Participant getUserById(int id) {
                return null;
            }

            @Override
            public void putUser(Participant user) {

            }
        };
        InetSocketAddress address = new InetSocketAddress("localhost", Const.SERVER_PORT);
        ConnectionHandler handler = ConnectionHandler.create(null, db);
        Assert.assertNotNull(handler);
    }

    @Before
    public void setup() {
        db = new IUserDB() {
            @Override
            public Participant getUserById(int id) {
                return null;
            }

            @Override
            public void putUser(Participant user) {

            }
        };
        InetSocketAddress address = new InetSocketAddress(addr, port);
        handler = ConnectionHandler.create(address, db);

        handler.launchServer();
    }

    @Test
    public void connect() {

        ConnectionHandler client = ConnectionHandler.create(new InetSocketAddress("localhost", Const.CLIENT_PORT), new IUserDB() {
            @Override
            public Participant getUserById(int id) {
                return null;
            }

            @Override
            public void putUser(Participant user) {

            }
        });

        InetSocketAddress address = new InetSocketAddress("localhost", Const.SERVER_PORT);
        client.connect(address);
    }

}
