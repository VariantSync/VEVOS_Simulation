package org.variantsync.vevos.simulation.feature.config;

import de.ovgu.featureide.fm.core.base.IFeature;
import org.prop4j.Node;
import org.tinylog.Logger;
import org.variantsync.vevos.simulation.feature.SimpleFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleConfiguration implements IConfiguration {
    private final Map<Object, Boolean> assignment;
    
    public SimpleConfiguration(final List<String> activeFeatures) {
        this.assignment = new HashMap<>();
        activeFeatures.forEach(f -> this.assignment.put(f, true));
        this.assignment.put("True", true);
        this.assignment.put("False", false);
    }
    
    @Override
    public boolean satisfies(final Node formula) {
        final List<String> containedFeatures = formula.getContainedFeatures();
        // Add features that are missing in the configuration as unset features
        containedFeatures.forEach(f -> {if (!assignment.containsKey(f)) {
            Logger.debug("Found a feature that was not defined previously: " + f);
            assignment.put(f, false);}
        });
        return formula.getValue(assignment);
    }

    @Override
    public List<IFeature> getFeatures() {
        List<IFeature> features = new ArrayList<>();
        for (Map.Entry<Object, Boolean> entry : this.assignment.entrySet()) {
            if (entry.getValue() && !entry.getKey().equals("True")) {
                features.add(new SimpleFeature((String) entry.getKey()));
            }
        }
        return features;
    }
}
