package org.variantsync.vevos.simulation.feature.sampling;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelElement;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.feature.config.SimpleConfiguration;
import org.variantsync.vevos.simulation.util.names.NameGenerator;
import org.variantsync.vevos.simulation.util.names.NumericNameGenerator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SimpleSampler implements Sampler {
    private final int sampleSize;
    private final int minFeatures;
    private final int maxFeatures;
    private final NameGenerator variantNameGenerator;

    public static SimpleSampler CreateRandomSampler(final int sampleSize) {
        return new SimpleSampler(sampleSize);
    }

    public static SimpleSampler CreateRandomSampler(final int sampleSize, final int minFeatures, final int maxFeatures) {
        return new SimpleSampler(sampleSize, minFeatures, maxFeatures);
    }

    public SimpleSampler(int size, int minFeatures, int maxFeatures) {
        this.sampleSize = size;
        this.minFeatures = minFeatures;
        this.maxFeatures = maxFeatures;
        this.variantNameGenerator = new NumericNameGenerator("Variant");
    }

    public SimpleSampler(int size) {
        this(size, 0, Integer.MAX_VALUE);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Sample sample(IFeatureModel model) {
        List<IFeature> features = new ArrayList<>(model.getFeatures());
        return sample(features);
    }

    public Sample sample(List<IFeature> features) {
        var random = new SecureRandom();
        if (features.size() > this.maxFeatures) {
            // Collect maxFeatures random features
            Collections.shuffle(features, random);
            features = features.subList(0, this.maxFeatures);
        }

        final AtomicInteger variantNo = new AtomicInteger();
        List<Variant> variants = new ArrayList<>(this.sampleSize);
        for (int i = 0; i < this.sampleSize; i++) {
            int numFeatures = random.nextInt(this.minFeatures, Integer.min(this.maxFeatures, features.size()));
            List<String> featureNames = features.subList(0, numFeatures).stream().map(IFeatureModelElement::getName).collect(Collectors.toList());
            variants.add(new Variant(this.variantNameGenerator.getNameAtIndex(variantNo.getAndIncrement()), new SimpleConfiguration(featureNames)));
            // Reshuffle for the next variant
            Collections.shuffle(features, random);
        }

        return new Sample(variants);
    }
}
