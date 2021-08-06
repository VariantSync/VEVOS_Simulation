package de.variantsync.evolution.feature;

import de.ovgu.featureide.fm.core.base.IFeatureModel;

import java.util.Map;

public interface Sampler {
    int size();
    Sample sample(IFeatureModel model);
    Sample sample(IFeatureModel model, Map<String, Boolean> fixedAssignment);
}
