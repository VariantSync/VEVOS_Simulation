package de.variantsync.evolution.blueprints;

import de.variantsync.evolution.VariantsCommit;
import de.variantsync.evolution.VariantsRevision;
import de.variantsync.feature.Sample;
import de.variantsync.feature.Variant;
import de.variantsync.repository.Branch;
import de.variantsync.repository.ISPLRepository;
import de.variantsync.repository.IVariantsRepository;
import de.variantsync.subjects.FeatureTraces;
import de.variantsync.subjects.SPLCommit;
import de.variantsync.subjects.VariabilityCommit;
import de.variantsync.util.functional.Lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VariantsRevisionFromVariabilityBlueprint extends VariantsRevisionBlueprint {
    private final VariabilityCommit variability;
    private final Optional<VariantsRevisionFromVariabilityBlueprint> predecessor;

    /**
     *
     * @param variabilityCommit The variability commit from which a VariantsRevision should be created.
     * @param predecessor may be null
     */
    public VariantsRevisionFromVariabilityBlueprint(
            VariabilityCommit variabilityCommit,
            VariantsRevisionFromVariabilityBlueprint predecessor)
    {
        this.variability = variabilityCommit;
        this.predecessor = Optional.ofNullable(predecessor);
    }

    public VariabilityCommit getVariabilityCommit() {
        return variability;
    }

    @Override
    protected Lazy<Sample> computeSample() {
        return variability.featureModel.map(featureModel -> {
            // TODO: Implement Issue #10 here.
            // If present, we can reuse predecessor to not have to compute a sample again.
            // For instance, when the feature model did not change during a commit, then
            // we can just return the sample from the previous revision (i.e.
            // predecessor.map(VariantsRevisionBlueprint::getSample).orElseGet(/*generate new sample here*/);
            // ).
            return null;
        });
    }

    @Override
    public Lazy<VariantsRevision.Branches> generateArtefactsFor(VariantsRevision revision) {
        return variability.featureTraces.and(getSample()).map(ts -> {
            final FeatureTraces traces = ts.getKey();
            final Sample sample = ts.getValue();
            final SPLCommit splCommit = variability.splCommit();
            final ISPLRepository splRepo = revision.getSPLRepo();
            final IVariantsRepository variantsRepo = revision.getVariantsRepo();

            final Map<Branch, VariantsCommit> commits = new HashMap<>(sample.size());
            for (Variant variant : sample.variants()) {
                final Branch branch = variantsRepo.getBranchByName(variant.name());
                variantsRepo.checkoutBranch(branch);
                splRepo.checkoutCommit(splCommit);
                // Generate the code
                traces.generateVariant(variant, splRepo, variantsRepo);
                // Commit the generated variant with the corresponding spl commit has as message.
                VariantsCommit variantsCommit = variantsRepo.commit(splCommit.id());
                commits.put(branch, variantsCommit);
            }

            return new VariantsRevision.Branches(commits);
        });
    }
}
