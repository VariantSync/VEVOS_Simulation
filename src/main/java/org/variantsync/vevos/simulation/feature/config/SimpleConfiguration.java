package org.variantsync.vevos.simulation.feature.config;

import org.prop4j.Node;
import org.tinylog.Logger;

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
}
