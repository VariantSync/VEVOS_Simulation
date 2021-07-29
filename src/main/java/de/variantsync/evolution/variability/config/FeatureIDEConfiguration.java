package de.variantsync.evolution.variability.config;

import de.ovgu.featureide.fm.core.ExtensionManager;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.impl.ConfigFormatManager;
import de.ovgu.featureide.fm.core.base.impl.ConfigurationFactoryManager;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.configuration.ConfigurationAnalyzer;
import de.ovgu.featureide.fm.core.configuration.ConfigurationPropagator;
import de.ovgu.featureide.fm.core.configuration.Selection;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Lazy;
import org.prop4j.Node;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for configurations from FeatureIDE.
 */
public class FeatureIDEConfiguration implements IConfiguration {
    private /* final */ Configuration featureIDEConfig;
    private final Lazy<Map<Object, Boolean>> asAssignment = Lazy.of(() -> {
        final Map<Object, Boolean> assignment = new HashMap<>();
        assignment.put(FixTrueFalse.True.var, true);
        assignment.put(FixTrueFalse.False.var, false);

        for (IFeature f : featureIDEConfig.getFeatureModel().getFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), false);
        }

        for (IFeature f : featureIDEConfig.getSelectedFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), true);
        }

        return assignment;
    });

    /**
     * Wrap the given FeatureIDE configuration.
     * @param featureIDEConfig Configuration in FeatureIDE format.
     */
    public FeatureIDEConfiguration(Configuration featureIDEConfig) {
        this.featureIDEConfig = featureIDEConfig;
    }

    /**
     * Create a minimal viable configuration such that all features in the given selection are active.
     * @param fm Feature model to satisfy.
     * @param activeFeatures Features to select.
     */
    public FeatureIDEConfiguration(FeatureModelFormula fm, List<String> activeFeatures) {
        this(new Configuration(fm));

        for (String activeFeature : activeFeatures) {
            featureIDEConfig.setManual(activeFeature, Selection.SELECTED);
        }

        // Selection might be incomplete (e.g., parent feature not selected)
        final ConfigurationAnalyzer analyzer = new ConfigurationAnalyzer(new ConfigurationPropagator(fm, featureIDEConfig));
        analyzer.completeMin();
    }

    /**
     * Load a configuration from the given path.
     * UNTESTED!
     * @param p Path to configuraton to load.
     * @throws ExtensionManager.NoSuchExtensionException If no loader for the given file type is registered in FeatureIDE.
     */
    public FeatureIDEConfiguration(Path p) throws ExtensionManager.NoSuchExtensionException {
        this(ConfigurationFactoryManager.getInstance().getFactory(p, ConfigFormatManager.getDefaultFormat()).create());
    }

    /**
     * Converts this configuration to an assignment from variables to values.
     * @return An assignment from variables to values.
     */
    public Map<Object, Boolean> toAssignment() {
        return asAssignment.run();
    }

    @Override
    public boolean satisfies(Node formula) {
        return formula.getValue(toAssignment());
    }
}
