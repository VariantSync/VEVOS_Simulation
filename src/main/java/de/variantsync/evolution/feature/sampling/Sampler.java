package de.variantsync.evolution.feature.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.feature.sampling.Sample;


public interface Sampler {
    int size();
    Sample sample(IFeatureModel model);
}
