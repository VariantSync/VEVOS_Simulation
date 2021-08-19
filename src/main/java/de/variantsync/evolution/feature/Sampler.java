package de.variantsync.evolution.feature;

import de.ovgu.featureide.fm.core.base.IFeatureModel;


public interface Sampler {
    int size();
    Sample sample(IFeatureModel model);
}
