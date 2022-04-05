package org.variantsync.vevos.simulation.variants.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import org.variantsync.vevos.simulation.feature.sampling.Sample;
import org.variantsync.vevos.simulation.variants.blueprints.VariantsRevisionBlueprint;

import java.util.Optional;

public interface SamplingStrategy {
    Sample sampleForRevision(Optional<IFeatureModel> model, VariantsRevisionBlueprint blueprint);
}
