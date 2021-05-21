package de.variantsync.evolution.util.fide;

import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConfigurationFactory;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.impl.ConfigurationFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.DefaultConfigurationFactory;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.configuration.ConfigurationAnalyzer;
import de.ovgu.featureide.fm.core.configuration.ConfigurationPropagator;
import de.ovgu.featureide.fm.core.configuration.Selection;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import org.prop4j.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationUtils {
    public static Map<Object, Boolean> ToAssignment(Configuration config) {
        final Map<Object, Boolean> assignment = new HashMap<>();
        assignment.put(FixTrueFalse.True.var, true);
        assignment.put(FixTrueFalse.False.var, false);

        for (IFeature f : config.getFeatureModel().getFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), false);
        }

        for (IFeature f : config.getSelectedFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), true);
        }

        return assignment;
    }

    public static boolean IsSatisfyingAssignment(Configuration config, Node formula) {
        return IsSatisfyingAssignment(ToAssignment(config), formula);
    }

    public static boolean IsSatisfyingAssignment(Map<Object, Boolean> assignment, Node formula) {
        return formula.getValue(assignment);
    }

    public static Configuration FromFeatureModelAndSelection(FeatureModelFormula fm, List<String> activeFeatures) {
        final Configuration configuration = new Configuration(fm);

        for (String activeFeature : activeFeatures) {
            configuration.setManual(activeFeature, Selection.SELECTED);
        }

        // Selection might be incomplete (e.g., parent feature not selected)
        final ConfigurationAnalyzer analyzer = new ConfigurationAnalyzer(new ConfigurationPropagator(fm, configuration));
        analyzer.completeMin();

        return configuration;
    }
}
