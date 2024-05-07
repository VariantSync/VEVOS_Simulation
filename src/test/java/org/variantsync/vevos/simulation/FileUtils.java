package org.variantsync.vevos.simulation;

import java.io.File;

import org.tinylog.Logger;

public class FileUtils {

    public static void removeFilesRecursively(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    removeFilesRecursively(file);
                }
            }
        } else if (directory.isFile()) {
            boolean deleted = directory.delete();
            if (deleted) {
                Logger.debug("Deleted file: {}", directory.getAbsolutePath());
            } else {
                Logger.debug("Failed to delete file: {}", directory.getAbsolutePath());
            }
        }
    }
}
