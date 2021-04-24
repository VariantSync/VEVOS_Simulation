package de.variantsync.evolution;

import de.variantsync.repository.Commit;
import de.variantsync.repository.IVariantsRepository;

public class VariantsCommit extends Commit<IVariantsRepository> {
    private String branch;

    public VariantsCommit(String commitId, String branch) {
        super(commitId);
    }
}
