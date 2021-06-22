package de.variantsync.evolution.variants.blueprints;

import de.variantsync.evolution.variants.VariantsRevision;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.util.functional.Lazy;

/**
 * A VariantsRevisionBlueprint holds instructions on how to generate variants and commits
 * for a VariantsRevision.
 * The main purpose of blueprints is to distinguish if variants should be generated as usual
 * (VariantsRevisionFormVariabilityBlueprint) or if we have to generate an artifical commit
 * for each variant indicating that an evolution history ends (VariantsRevisionFromErrorBlueprint).
 */
public abstract class VariantsRevisionBlueprint {
    // We wrap computeSample in another Lazy to cache the result of computeSample (which is yet another lazy).
    // Assume computeSample returns a new sample each time it is called (which could happen).
    // Then whenever we do computeSample().run(), the sample would be calculated anew which could be costly.
    // Thus, we cache the lazy that computes the sample (i.e., computeSample()).
    private final Lazy<Lazy<Sample>> sample = Lazy.of(this::computeSample);

    /**
     * @return The sample of variants that should be generated for target VariantsRevision.
     */
    public Lazy<Sample> getSample() {
        return sample.run();
    }

    /**
     * @return A computation yielding the sample. The returned lazy is cached and returned whenever getSample is called.
     */
    protected abstract Lazy<Sample> computeSample();

    /**
     * Generates the variants for the given VariantsRevision and commits them to the corresponding AbstractVariantsRepository.
     * @param revision The revision for which variants should be generated to disk.
     * @return A computation that when run, generates the variants and returns the generated commits.
     */
    public abstract Lazy<VariantsRevision.Branches> generateArtefactsFor(VariantsRevision revision);
}
