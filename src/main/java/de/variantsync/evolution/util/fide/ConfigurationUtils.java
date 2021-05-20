package de.variantsync.evolution.util.fide;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import org.prop4j.Node;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationUtils {
    public static boolean isSatisfyingAssignment(Configuration config, Node formula) {
        if (config == null) {
            return true;
        }

        // TODO: Use Analyzer.isValid and canBeValid

        Map<Object, Boolean> assignment = new HashMap<>();

        for (IFeature f : config.getFeatureModel().getFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), false);
        }

        for (IFeature f : config.getSelectedFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), true);
        }

        return formula.getValue(assignment);
    }
}
