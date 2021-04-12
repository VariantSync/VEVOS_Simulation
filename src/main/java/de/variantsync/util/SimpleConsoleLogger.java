package de.variantsync.util;

import java.util.HashMap;

public class SimpleConsoleLogger extends Logger {

    protected SimpleConsoleLogger() {
        this.streamMap = new HashMap<>();
        this.streamMap.put(LogLevel.DEBUG, System.out);
        this.streamMap.put(LogLevel.INFO, System.out);
        this.streamMap.put(LogLevel.STATUS, System.out);
        this.streamMap.put(LogLevel.WARNING, System.out);
        this.streamMap.put(LogLevel.ERROR, System.err);
    }
}
