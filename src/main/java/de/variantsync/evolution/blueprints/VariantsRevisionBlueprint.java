package de.variantsync.evolution.blueprints;

import de.variantsync.evolution.VariantsRevision;
import de.variantsync.feature.Sample;
import de.variantsync.util.functional.Lazy;

public abstract class VariantsRevisionBlueprint {
    private Lazy<Sample> sample = null;

    public Lazy<Sample> getSample() {
        if (sample == null) {
            sample = computeSample();
        }

        return sample;
    }

    protected abstract Lazy<Sample> computeSample();

    public abstract Lazy<VariantsRevision.Branches> generateArtefactsFor(VariantsRevision revision);
}
