package org.variantsync.vevos.simulation.repository;

import org.variantsync.vevos.simulation.variability.SPLCommit;

import java.nio.file.Path;

/**
 * Repository containing a preprocessor based software product line (e.g., Linux kernel or Maven).
 */
public abstract class AbstractSPLRepository extends Repository<SPLCommit> {
    public AbstractSPLRepository(final Path path) {
        super(path);
    }

    @Override
    public SPLCommit idToCommit(final String id) {
        return new SPLCommit(id);
    }
}
