package de.variantsync.evolution.variants.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.variants.blueprints.VariantsRevisionBlueprint;

import java.util.Optional;

public interface SamplingStrategy {
    Sample sampleForRevision(Optional<IFeatureModel> model, VariantsRevisionBlueprint blueprint);
}
