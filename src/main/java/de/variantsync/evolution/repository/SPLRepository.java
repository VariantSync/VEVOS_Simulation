package de.variantsync.evolution.repository;

import de.variantsync.evolution.variability.SPLCommit;

import java.nio.file.Path;

public class SPLRepository extends Repository<SPLCommit> implements ISPLRepository {

    public SPLRepository(Path path){
        super(path);
    }

    @Override
    public SPLCommit getCurrentCommit() {
        String id = getCurrentCommitId();
        return new SPLCommit(id);
    }
}
