package de.variantsync.evolution.feature.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.Sampler;
import de.variantsync.evolution.util.NotImplementedException;

public class UniformRandomSampling extends ResizableSampler {
    public UniformRandomSampling(final int size) {
        super(size);
    }

    @Override
    public Sample sample(final IFeatureModel model) {
        throw new NotImplementedException();
    }
}
