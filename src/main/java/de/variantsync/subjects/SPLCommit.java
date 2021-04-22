package de.variantsync.subjects;

import de.variantsync.repository.Commit;
import de.variantsync.repository.ISPLRepository;

public class SPLCommit extends Commit<ISPLRepository> {
    public SPLCommit(String commitId) {
        super(commitId);
    }
}
