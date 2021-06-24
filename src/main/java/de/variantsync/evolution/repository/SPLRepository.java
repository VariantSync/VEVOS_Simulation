package de.variantsync.evolution.repository;

import de.variantsync.evolution.variability.SPLCommit;

import java.nio.file.Path;

/**
 * Repository containing a preprocessor based software product line (e.g., Linux kernel or Maven).
 */

public class SPLRepository extends AbstractSPLRepository {
    public SPLRepository(Path path){
        super(path);
    }
}
