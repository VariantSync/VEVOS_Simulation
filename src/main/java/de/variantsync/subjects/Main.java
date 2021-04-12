package de.variantsync.subjects;

import de.variantsync.util.Logger;
import de.variantsync.util.SimpleConsoleLogger;

public class Main {

    static {
        Logger.init(SimpleConsoleLogger.class);
    }

    public static void main(String... args) {
        System.out.println("Hello world!");
        Logger.debug("DEBUG-TEST");
        Logger.info("INFO-TEST");
        Logger.status("STATUS-TEST");
        Logger.warning("WARNING-TEST");
        Logger.error("ERROR-TEST");
    }
}
