package com.alphagfx.common;

// TODO: 07/03/18 move to properties or other type of config file
public class Const {
    //Default message encoding
    public static final String CHARSET = "UTF-8";
    public static final int READ_BUFFER_SIZE = 2048;

    //Amount of threads for AsyncChannelGroup
    public static final int THREADS_PER_GROUP = 10;

    //Time to connect remote server in seconds
    public static final int CONNECTION_TIMEOUT = 10;

    public static int CLIENT_PORT = 8888;
    public static int SERVER_PORT = 8889;
}
