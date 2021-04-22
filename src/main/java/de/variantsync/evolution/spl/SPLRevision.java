package de.variantsync.evolution.spl;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.variantsync.evolution.variant.VariantRevision;
import de.variantsync.evolution.variant.VariantsRevision;
import de.variantsync.subjects.SPLCommit;
import de.variantsync.subjects.VariabilityCommit;
import de.variantsync.util.Functional;
import de.variantsync.util.NotImplementedException;

import java.util.List;
import java.util.Optional;

public class SPLRevision {
    private final IFeatureModel featureModel;
    private final SPLCommit splCommit; // e.g. commit hash in linux history
    private final VariabilityCommit datasetCommit; // commit hash in the history of our variability repository
    // + variability
    private SPLRevision successor = null; // make lazy because expensive

    public SPLRevision(IFeatureModel featureModel, SPLCommit splCommit, VariabilityCommit datasetCommit) {
        this.featureModel = featureModel;
        this.splCommit = splCommit;
        this.datasetCommit = datasetCommit;
    }

    public List<Configuration> sample(int numSamples) {
        // TODO: Do uniform random sampling, probably with seed so we can recreate samples
        throw new NotImplementedException();
    }

    public VariantsRevision deriveVariants(List<Configuration> sample) {
        return new VariantsRevision(this, Functional.fmap(sample, this::generateVariant));
    }

    public VariantRevision generateVariant(Configuration config) {
        // TODO: Integrate traceability and artefacts
        return new VariantRevision(config, null);
    }

    public Optional<SPLRevision> getSuccessor() {
        if (successor != null) {
            return Optional.of(successor);
        }

        // TODO: compute successor and return it

        /*
         * Return empty when the successor is
         * - a merge commit
         * - an invalid commit (error occured during variability extraction)
         * - not existing
         */
        return Optional.empty();
    }
}
