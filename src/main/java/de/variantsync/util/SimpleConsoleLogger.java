package de.variantsync.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import static de.variantsync.util.LogLevel.*;

public class SimpleConsoleLogger {
    private final String name;
    private static LogLevel logLevel = INFO;

    private SimpleConsoleLogger(String name) {
        this.name = name;
    }

    public static SimpleConsoleLogger get() {
        return new SimpleConsoleLogger(Reflection.getCallerClassName());
    }

    public static SimpleConsoleLogger get(String name) {
        return new SimpleConsoleLogger(name);
    }

    public void debug(String message) {
        if (logLevel.ordinal() >= DEBUG.ordinal()) {
            System.out.println(format(message, DEBUG));
        }
    }

    public void info(String message) {
        if (logLevel.ordinal() >= INFO.ordinal()) {
            System.out.println(format(message, INFO));
        }
    }

    public <T> void info(Collection<T> collection) {
        info(collectionToString(collection));
    }

    public void status(String message) {
        if (logLevel.ordinal() >= STATUS.ordinal()) {
            System.out.println(format(message, STATUS));
        }
    }

    public void warning(String message) {
        if (logLevel.ordinal() >= WARNING.ordinal()) {
            System.out.println(format(message, WARNING));
        }
    }

    public void error(String message) {
        if (logLevel.ordinal() >= ERROR.ordinal()) {
            System.err.println(format(message, ERROR));
        }
    }

    public <T> void error(Collection<T> collection) {
        error(collectionToString(collection));
    }

    public void exception(String message, Exception e) {
        if (logLevel.ordinal() >= ERROR.ordinal()) {
            System.err.println(format(message + "\n" + e.getMessage(), ERROR));
        }
    }

    public static void setLogLevel(LogLevel level) {
        SimpleConsoleLogger.logLevel = level;
    }

    public static LogLevel logLevel() {
        return SimpleConsoleLogger.logLevel;
    }

    private <T> String collectionToString(Collection<T> collection) {
        StringBuilder sb = new StringBuilder("[");
        int count = 0;
        for (var item : collection) {
            sb.append(item);
            if (count < collection.size() - 1) {
                sb.append(" , ");
            }
            count++;
        }
        sb.append("]");
        return sb.toString();
    }


    private String format(String message, LogLevel level) {
        return String.format("[%s] [%s] [%s] [%s] %s", LocalDateTime.now().format(DateTimeFormatter.ISO_TIME), level, Thread.currentThread().getName(), name, message);
    }

}
