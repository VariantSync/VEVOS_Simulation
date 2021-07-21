package de.variantsync.evolution.variants.sampling;

import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.feature.sampling.ConstSampler;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.Sampler;
import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.functional.Functional;
import de.variantsync.evolution.variants.blueprints.VariantsRevisionBlueprint;
import org.prop4j.Node;

import java.util.Optional;

public class SampleOnceAtBeginStrategy implements SamplingStrategy {
    private final Sampler sampler;

    public SampleOnceAtBeginStrategy(Sampler sampler) {
        this.sampler = sampler;
    }

    @Override
    public Sample sample(IFeatureModel model) {
        return sampler.sample(model);
    }

    @Override
    public Sample sampleForRevision(Optional<IFeatureModel> model, VariantsRevisionBlueprint blueprint) {
        return Functional.match(blueprint.getPredecessor(),
                // Use the sample we already computed.
                p -> {
                    final Sample previousSample = p.getSample().run();

                    // If we have a feature model, validate that the previous sample is still valid.
                    model.ifPresent(fm -> {
                        final Node featureModelFormula = new FeatureModelFormula(fm).getPropositionalNode();
                        for (Variant variant : previousSample.variants()) {
                            if (!variant.getConfiguration().satisfies(featureModelFormula)) {
                                throw new IllegalSampleException("Sampled " + variant + " is not valid anymore for feature model " + model + "!");
                            }
                        }
                    });

                    return previousSample;
                },
                // If there is no predecessor the given blueprint is the first one.
                () -> Functional.match(model,
                        this::sample,
                        () -> {
                            if (sampler instanceof ConstSampler c) {
                                return c.sample();
                            }
                            throw new IllegalStateException("The given blueprint has neither a predecessor (thus it is assumed it is the first blueprint) nor a feature model (that could be sampled). The given sampler also is not a ConstSampler, thus no sample could be determined! Either provide a previous valid blueprint, a feature model, or a ConstSampler!");
                        }
                )
        );
    }
}
