package de.variantsync.evolution.variants.blueprints;

import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.variability.pc.Artefact;
import de.variantsync.evolution.variants.VariantCommit;
import de.variantsync.evolution.variants.VariantsRevision;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.repository.Branch;
import de.variantsync.evolution.repository.AbstractSPLRepository;
import de.variantsync.evolution.repository.AbstractVariantsRepository;
import de.variantsync.evolution.variability.SPLCommit;
import de.variantsync.evolution.variability.VariabilityCommit;
import de.variantsync.evolution.util.functional.Lazy;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default blueprint to generate variants from the software product line at a certain commit.
 */
public class VariantsRevisionFromVariabilityBlueprint extends VariantsRevisionBlueprint {
    private final VariabilityCommit variability;
    private final Optional<VariantsRevisionFromVariabilityBlueprint> predecessor;

    /**
     * Creates a new blueprint that can generate variants from the given variabilityCommit that contains
     * the variability information of a specific SPLCommit.
     * @param variabilityCommit The variability commit from which a VariantsRevision should be created.
     * @param predecessor The predecessor blueprint that generates the VariantsRevision that has to be generated before
     *                    the revision of this blueprint. May be null, if this is the first blueprint to generate.
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
        return variability.presenceConditions.and(getSample()).map(ts -> {
            final Artefact traces = ts.getKey();
            final Sample sample = ts.getValue();
            final SPLCommit splCommit = variability.splCommit();
            final AbstractSPLRepository splRepo = revision.getSPLRepo();
            final AbstractVariantsRepository variantsRepo = revision.getVariantsRepo();

            final Map<Branch, VariantCommit> commits = new HashMap<>(sample.size());
            for (Variant variant : sample.variants()) {
                final Branch branch = variantsRepo.getBranchByName(variant.getName());

                try {
                    variantsRepo.checkoutBranch(branch);
                } catch (IOException | GitAPIException e) {
                    throw new RuntimeException("Failed checkout of branch " + branch + " in variants repository.");
                }

                try {
                    splRepo.checkoutCommit(splCommit);
                } catch (IOException | GitAPIException e) {
                    throw new RuntimeException("Failed checkout of commit " + splCommit.id() + " in SPL Repository.");
                }

                // Generate the code
                final Result<? extends Artefact, Exception> result = traces.generateVariant(
                        variant,
                        new CaseSensitivePath(splRepo.getPath()),
                        new CaseSensitivePath(variantsRepo.getPath()));
                Logger.log(result.map(u -> "Generating variant " + variant + " was successful!"));

                // Commit the generated variant with the corresponding spl commit has as message.
                final String commitMessage = splCommit.id() + " || " + splCommit.message() + " || " + variant.getName();
                final Optional<VariantCommit> variantCommit;

                try {
                    variantCommit = variantsRepo.commit(commitMessage);
                } catch (GitAPIException | IOException e) {
                    throw new RuntimeException("Failed to commit " + commitMessage + " to VariantsRepository.");
                }

                variantCommit.ifPresent(commit -> commits.put(branch, commit));
            }

            return new VariantsRevision.Branches(commits);
        });
    }
}
