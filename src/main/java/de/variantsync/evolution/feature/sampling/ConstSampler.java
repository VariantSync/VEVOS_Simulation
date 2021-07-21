package de.variantsync.evolution.feature.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.Sampler;

public final record ConstSampler(Sample sample) implements Sampler {
    @Override
    public Sample sample(IFeatureModel model) {
        return sample;
    }
}
