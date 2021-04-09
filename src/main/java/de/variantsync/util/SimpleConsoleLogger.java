package de.variantsync.util;

import java.time.LocalDateTime;
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

    public void info(String message) {
        if (logLevel == INFO) {
            System.out.println(format(message, INFO));
        }
    }

    public <T> void info(Collection<T> collection) {
        info(collectionToString(collection));
    }

    public void status(String message) {
        if (logLevel == INFO || logLevel == STATUS) {
            System.out.println(format(message, STATUS));
        }
    }

    public void warning(String message) {
        if (logLevel == INFO || logLevel == STATUS || logLevel == WARNING) {
            System.out.println(format(message, WARNING));
        }
    }

    public void error(String message) {
        System.err.println(format(message, INFO));
    }

    public <T> void error(Collection<T> collection) {
        error(collectionToString(collection));
    }

    public void exception(String message, Exception e) {
        System.err.println(format(message + "\n" + e.getMessage(), INFO));
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
        return String.format("[%s]  [%s]  [%s]  [%s]  %s", LocalDateTime.now(), level, Thread.currentThread().getId(), name, message);
    }

}
