package de.variantsync.evolution.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.variantsync.evolution.util.LogLevel.*;
import static de.variantsync.evolution.util.LogLevel.ERROR;


public class Logger {
    protected static LogLevel logLevel = INFO;
    private static Logger INSTANCE;
    private final Map<LogLevel, OutputStream> streamMap;

    private Logger(Map<LogLevel, OutputStream> streamMap) {
        this.streamMap = streamMap;
    }

    public static void init(Map<LogLevel, OutputStream> streamMap) {
        if (Logger.INSTANCE != null) {
            throw new RuntimeException("Logger already initialized.");
        }
        for (LogLevel level : LogLevel.values()) {
            if (!streamMap.containsKey(level)) {
                throw new IllegalArgumentException("No Output stream for " + logLevel + " defined.");
            }
        }
        Logger.INSTANCE = new Logger(streamMap);
    }

    public static void initConsoleLogger() {
        Map<LogLevel, OutputStream> streamMap = new HashMap<>();
        streamMap.put(LogLevel.INFO, System.out);
        streamMap.put(LogLevel.DEBUG, System.out);
        streamMap.put(LogLevel.STATUS, System.out);
        streamMap.put(LogLevel.WARNING, System.out);
        streamMap.put(LogLevel.ERROR, System.err);
        init(streamMap);
    }

    public static void debug(String message) {
        INSTANCE.log(message, DEBUG);
    }

    public static void info(String message) {
        INSTANCE.log(message, INFO);
    }

    public static <T> void info(Collection<T> collection) {
        info(collectionToString(collection));
    }

    public static void status(String message) {
        INSTANCE.log(message, STATUS);
    }

    public static void warning(String message) {
        INSTANCE.log(message, WARNING);
    }

    public static void error(String message) {
        INSTANCE.log(message, ERROR);
    }

    public static <T> void error(Collection<T> collection) {
        error(collectionToString(collection));
    }

    public static void exception(String message, Exception e) {
        INSTANCE.log(message + "\n" + e.getMessage(), ERROR);
    }

    public static void setLogLevel(LogLevel level) {
        Logger.logLevel = level;
    }

    public static LogLevel logLevel() {
        return Logger.logLevel;
    }

    private static <T> String collectionToString(Collection<T> collection) {
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

    protected void log(String message, LogLevel targetLevel) {
        if (logLevel.ordinal() <= targetLevel.ordinal()) {
            try {
                var stream = streamMap.get(targetLevel);
                stream.write(format(message, targetLevel).getBytes(StandardCharsets.UTF_8));
                stream.write('\n');
                stream.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    // https://stackoverflow.com/questions/11306811/how-to-get-the-caller-class-in-java
    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().startsWith("java.lang.Thread")) {
                if (callerClassName == null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName())) {
                    String[] name = ste.getClassName().split("\\.");
                    return name[name.length - 1];
                }
            }
        }
        return null;
    }

    private static String format(String message, LogLevel level) {
        return String.format("[%s] [%s] [%s] [%s] %s", LocalDateTime.now().format(DateTimeFormatter.ISO_TIME), level, Thread.currentThread().getName(), getCallerClassName(), message);
    }

}
