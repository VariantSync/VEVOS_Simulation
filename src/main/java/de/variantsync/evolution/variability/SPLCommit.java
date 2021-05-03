package de.variantsync.evolution.variability;

import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.ISPLRepository;

public class SPLCommit extends Commit<ISPLRepository> {
    private final String message;

    public SPLCommit(String commitId) {
        // TODO: Implement Issue #13 here.
        this(commitId, "");
    }

    public SPLCommit(String commitId, String message) {
        super(commitId);
        this.message = message;
    }

    public String message() {
        return message;
    }
}
