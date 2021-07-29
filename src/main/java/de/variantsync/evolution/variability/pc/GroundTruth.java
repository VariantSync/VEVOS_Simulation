package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.util.functional.Functor;

import java.util.function.Function;

public record GroundTruth<A extends Artefact>(A variantArtefact, BlockMatching matching) {
    public <B extends Artefact> GroundTruth<B> map(Function<? super A, ? extends B> f) {
        return new GroundTruth<>(f.apply(variantArtefact), matching);
    }
}
