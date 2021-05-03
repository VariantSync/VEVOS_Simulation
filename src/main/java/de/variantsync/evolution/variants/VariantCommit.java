package de.variantsync.evolution.variants;

import de.variantsync.evolution.repository.Branch;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.IVariantsRepository;

/**
 * Represents a commit to an IVariantsRepository.
 */
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
