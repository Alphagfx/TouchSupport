package com.alphagfx.common;

import com.alphagfx.common.connection.ConnectionHandlerAsync;
import com.alphagfx.common.database.UserDatabase;

import java.util.HashMap;
import java.util.Map;

public class ConnectionHandlerTest {

    private UserDatabase db;
    private ConnectionHandlerAsync handler;

    private Map<Integer, Participant> connectedUsers = new HashMap<>();

    private String addr = "localhost";
    private int port = Const.SERVER_PORT;

    // TODO: 31/05/18 create Tests


}
