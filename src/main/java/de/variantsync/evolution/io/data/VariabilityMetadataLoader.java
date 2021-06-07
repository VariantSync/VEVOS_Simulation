package de.variantsync.evolution.io.data;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.functional.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VariabilityMetadataLoader implements ResourceLoader<VariabilityMetadata> {
    private final static String SUCCESS_COMMIT_FILE = "SUCCESS_COMMITS.txt";
    private final static String ERROR_COMMIT_FILE = "ERROR_COMMITS.txt";
    private final static String INCOMPLETE_PC_COMMIT_FILE = "INCOMPLETE_PC_COMMITS.txt";


    /**
     *
     * @param p The path which should be checked.
     * @return true if the path points to a directory that contains at least one of the metadata files, otherwise false.
     */
    @Override
    public boolean canLoad(Path p) {
        try {
            return Files.list(p)
                    .map(Path::toFile)
                    .anyMatch(f -> {
                        String name = f.getName();
                        return name.equals(SUCCESS_COMMIT_FILE) || name.equals(ERROR_COMMIT_FILE) || name.equals(INCOMPLETE_PC_COMMIT_FILE);
                    });
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Result<VariabilityMetadata, Exception> load(Path p) {
        return null;
    }
}
