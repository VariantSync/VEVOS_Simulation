package de.variantsync.evolution.feature.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.Sampler;

public final record ConstSampler(Sample sample) implements Sampler {
    @Override
    public int size() {
        return sample.size();
    }

    @Override
    public Sample sample(final IFeatureModel model) {
        return sample;
    }
}
