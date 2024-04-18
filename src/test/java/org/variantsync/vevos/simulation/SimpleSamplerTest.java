package org.variantsync.vevos.simulation;

import de.ovgu.featureide.fm.core.base.IFeature;
import org.junit.Assert;
import org.junit.Test;
import org.variantsync.vevos.simulation.feature.SimpleFeature;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.feature.sampling.Sample;
import org.variantsync.vevos.simulation.feature.sampling.SimpleSampler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleSamplerTest {

    private static final List<IFeature> features = Stream.of("A", "B", "C", "D").map(SimpleFeature::new).collect(Collectors.toList());
    private static final int TEST_RUNS = 100;

    @Test
    public void fewerFeatures() {
        SimpleSampler sampler = SimpleSampler.CreateRandomSampler(3,3, 5);

        sampleTest(sampler,3, 5);
    }

    private static void sampleTest(SimpleSampler sampler, int min, int max) {
        for (int i = 0; i < TEST_RUNS; i++) {
            Sample sample = sampler.sample(features);
            Assert.assertEquals(sample.size(), 3);
            for (Variant variant : sample) {
                Assert.assertTrue(variant.getConfiguration().getFeatures().size() <= max);
                Assert.assertTrue(variant.getConfiguration().getFeatures().size() >= min);
            }
        }
    }

    @Test
    public void exactNumberOfFeatures() {
        SimpleSampler sampler = SimpleSampler.CreateRandomSampler(3,2, 4);
        sampleTest(sampler,2, 4);
    }

    @Test
    public void moreFeatures() {
        SimpleSampler sampler = SimpleSampler.CreateRandomSampler(3,1, 2);
        sampleTest(sampler, 1,3);
    }
}
