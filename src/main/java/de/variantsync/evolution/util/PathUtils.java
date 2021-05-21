package de.variantsync.evolution.util;

import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collector;
import java.util.stream.Stream;

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

    public static boolean createEmpty(Path p) throws IOException {
        return createEmpty(p.toFile());
    }

    public static boolean createEmpty(File f) throws IOException {
        f.getParentFile().mkdirs();
        return f.createNewFile();
    }

    public static Result<Unit, IOException> deleteDirectory(Path path) {
        // read java doc, Files.walk need close the resources.
        // try-with-resources to ensure that the stream's open directories are closed
        try (Stream<Path> walk = Files.walk(path)) {
            return walk
                    .sorted(Comparator.reverseOrder())
                    .map(f -> Result.<IOException>Try(() -> Files.delete(f)))
                    .reduce((a, b) -> {
                        if (a.isFailure()) {
                            return a;
                        }
                        if (b.isFailure()) {
                            return b;
                        }
                        return a;
                    }).orElseGet(() -> Result.Success(Unit.Instance()));
        } catch (IOException e) {
            return Result.Failure(e);
        }
    }
}
