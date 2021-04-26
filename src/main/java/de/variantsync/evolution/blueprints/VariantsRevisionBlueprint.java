package de.variantsync.evolution.blueprints;

import de.variantsync.evolution.VariantsRevision;
import de.variantsync.feature.Sample;
import de.variantsync.util.functional.Lazy;

public abstract class VariantsRevisionBlueprint {
    private Lazy<Sample> sample = null;

    protected abstract Lazy<Sample> computeSample();

    public Lazy<Sample> getSample() {
        if (sample == null) {
            sample = computeSample();
        }

        return sample;
    }

    public abstract Lazy<VariantsRevision.Branches> generateArtefactsFor(VariantsRevision revision);
}
