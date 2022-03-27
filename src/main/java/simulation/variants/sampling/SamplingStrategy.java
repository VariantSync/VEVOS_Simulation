package simulation.variants.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import simulation.feature.sampling.Sample;
import simulation.variants.blueprints.VariantsRevisionBlueprint;

import java.util.Optional;

public interface SamplingStrategy {
    Sample sampleForRevision(Optional<IFeatureModel> model, VariantsRevisionBlueprint blueprint);
}
