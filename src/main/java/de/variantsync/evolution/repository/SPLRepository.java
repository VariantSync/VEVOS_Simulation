package de.variantsync.evolution.repository;

import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.variability.SPLCommit;
import de.variantsync.evolution.variants.VariantCommit;

import java.io.IOException;
import java.nio.file.Path;

public class SPLRepository extends Repository<SPLCommit> implements ISPLRepository {

    public SPLRepository(Path path){
        super(path);
    }

    @Override
    public SPLCommit getCurrentCommit() throws IOException {
        try {
            String id = getCurrentCommitId();
            return new SPLCommit(id);
        } catch (IOException e) {
            Logger.exception("Failed to get current commit.", e);
            throw e;
        }
    }
}
