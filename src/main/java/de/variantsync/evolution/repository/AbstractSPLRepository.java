package de.variantsync.evolution.repository;

import de.variantsync.evolution.variability.SPLCommit;

import java.nio.file.Path;

/**
 * Repository containing a preprocessor based software product line (e.g., Linux kernel or Maven).
 */
public abstract class AbstractSPLRepository extends Repository<SPLCommit> {
    public AbstractSPLRepository(Path path) {
        super(path);
    }

    @Override
    public SPLCommit idToCommit(String id) {
        return new SPLCommit(id);
    }
}
