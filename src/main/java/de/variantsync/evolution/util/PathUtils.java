package de.variantsync.evolution.util;

import java.nio.file.Path;
import java.util.Locale;

public class PathUtils {
    public static boolean hasExtension(Path path, String... extensions) {
        final String p = path.toString().toLowerCase(Locale.ROOT);
        for (String extension : extensions) {
            if (p.endsWith(extension.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
