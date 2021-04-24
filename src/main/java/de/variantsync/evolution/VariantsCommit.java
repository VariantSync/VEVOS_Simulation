package de.variantsync.evolution;

import de.variantsync.repository.Branch;
import de.variantsync.repository.Commit;
import de.variantsync.repository.IVariantsRepository;

public class VariantsCommit extends Commit<IVariantsRepository> {
    private Branch branch;

    public VariantsCommit(String commitId, Branch branch) {
        super(commitId);
    }
}
