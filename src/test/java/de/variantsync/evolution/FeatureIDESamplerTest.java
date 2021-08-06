package de.variantsync.evolution;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.config.FeatureIDEConfiguration;
import de.variantsync.evolution.feature.sampling.FeatureIDESampler;
import de.variantsync.evolution.util.fide.FeatureModelUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class FeatureIDESamplerTest {

    static {
        Main.Initialize();
    }
    
    @Test
    public void sampleWithFixedAllFalse() {
        IFeatureModel model = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");

        Map<String, Boolean> fixedAssignment = new HashMap<>();
        fixedAssignment.put("A", false);
        fixedAssignment.put("B", false);
        fixedAssignment.put("C", false);
        fixedAssignment.put("D", false);
        fixedAssignment.put("E", false);
        
        FeatureIDESampler sampler = FeatureIDESampler.CreateRandomSampler(1);
        Sample sample = sampler.sample(model, fixedAssignment);
        FeatureIDEConfiguration configuration =  (FeatureIDEConfiguration) sample.variants().get(0).getConfiguration();
        assert configuration.getConfiguration().getSelectedFeatures().size() == 1;
        assert configuration.getConfiguration().getSelectedFeatures().get(0).getName().equals("Root");
    }

    @Test
    public void sampleWithFixedAllTrue() {
        IFeatureModel model = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");

        Map<String, Boolean> fixedAssignment = new HashMap<>();
        fixedAssignment.put("A", true);
        fixedAssignment.put("B", true);
        fixedAssignment.put("C", true);
        fixedAssignment.put("D", true);
        fixedAssignment.put("E", true);

        FeatureIDESampler sampler = FeatureIDESampler.CreateRandomSampler(1);
        Sample sample = sampler.sample(model, fixedAssignment);
        FeatureIDEConfiguration configuration =  (FeatureIDEConfiguration) sample.variants().get(0).getConfiguration();
        assert configuration.getConfiguration().getSelectedFeatures().containsAll(model.getFeatures());
    }

    @Test
    public void sampleWithFixed() {
        IFeatureModel model = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");

        Map<String, Boolean> fixedAssignment = new HashMap<>();
        fixedAssignment.put("A", true);
        fixedAssignment.put("B", false);
        fixedAssignment.put("C", true);
        fixedAssignment.put("D", false);
        fixedAssignment.put("E", true);

        FeatureIDESampler sampler = FeatureIDESampler.CreateRandomSampler(1);
        Sample sample = sampler.sample(model, fixedAssignment);
        FeatureIDEConfiguration configuration =  (FeatureIDEConfiguration) sample.variants().get(0).getConfiguration();
        Map<Object, Boolean> assignment = configuration.toAssignment();
        assert assignment.get("A");
        assert !assignment.get("B");
        assert assignment.get("C");
        assert !assignment.get("D");
        assert assignment.get("E");
    }
}
