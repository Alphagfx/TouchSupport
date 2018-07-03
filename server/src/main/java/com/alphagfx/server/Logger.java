package com.alphagfx.server;

public class Logger {
    private static final Logger log = new Logger();
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("name");

    public static void info(String message) {
        info(message, null);
    }

    public static void info(String message, Throwable e) {
        logger.info(message, e);
    }

    public static void warn(String message) {
        warn(message, null);
    }

    public static void warn(String message, Throwable e) {
        logger.warn(message, e);
    }

    public static void error(String message) {
        error(message, null);
    }

    public static void error(String message, Throwable e) {
        logger.error(message, e);
    }

    public static void fatal(String message) {
        fatal(message, null);
    }

    public static void fatal(String message, Throwable e) {
        logger.fatal(message, e);
    }

    public static Logger getInstance() {
        return log;
    }
}
