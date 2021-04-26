package de.variantsync.evolution.blueprints;

import de.variantsync.evolution.VariantsCommit;
import de.variantsync.evolution.VariantsRevision;
import de.variantsync.feature.Sample;
import de.variantsync.feature.Variant;
import de.variantsync.repository.Branch;
import de.variantsync.repository.IVariantsRepository;
import de.variantsync.util.functional.Lazy;

import java.util.HashMap;
import java.util.Map;

public class VariantsRevisionFromErrorBlueprint extends VariantsRevisionBlueprint {
    public final String COMMIT_MESSAGE = "SUB_HISTORY_END";
    private final VariantsRevisionFromVariabilityBlueprint predecessor;

    /**
     * Creates a new error blueprint for a VariantsRevision.
     * An error revision has always to be preceded by a normal blueprint.
     * (Otherwise the error blueprint would be pointless).
     * @param predecessor The blueprint for the previous revision. Cannot be null.
     */
    public VariantsRevisionFromErrorBlueprint(VariantsRevisionFromVariabilityBlueprint predecessor) {
        this.predecessor = predecessor;
    }

    @Override
    protected Lazy<Sample> computeSample() {
        return predecessor.getSample();
    }

    @Override
    public Lazy<VariantsRevision.Branches> generateArtefactsFor(VariantsRevision revision) {
        return getSample().map(sample -> {
            final IVariantsRepository variantsRepo = revision.getVariantsRepo();
            final Map<Branch, VariantsCommit> commits = new HashMap<>(sample.size());

            for (Variant variant : sample.variants()) {
                final Branch branch = variantsRepo.getBranchByName(variant.name());
                variantsRepo.checkoutBranch(branch);
                // TODO: We cannot commit no changes. So we have to change something. What could that be?
                VariantsCommit variantsCommit = variantsRepo.commit(COMMIT_MESSAGE);
                commits.put(branch, variantsCommit);
            }

            return new VariantsRevision.Branches(commits);
        });
    }
}
