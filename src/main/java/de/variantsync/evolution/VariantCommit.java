package de.variantsync.evolution;

import de.variantsync.repository.Branch;
import de.variantsync.repository.Commit;
import de.variantsync.repository.IVariantsRepository;

public class VariantCommit extends Commit<IVariantsRepository> {
    private final Branch branch;

    public VariantCommit(String commitId, Branch branch) {
        super(commitId);
        this.branch = branch;
    }

    public Branch branch() {
        return branch;
    }
}
