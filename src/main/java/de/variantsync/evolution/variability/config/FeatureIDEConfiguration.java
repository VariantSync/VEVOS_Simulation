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
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Lazy;
import org.prop4j.Node;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureIDEConfiguration implements IConfiguration {
    private Configuration featureIDEConfig;
    private Lazy<Map<Object, Boolean>> asAssignment = Lazy.of(() -> {
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

    public FeatureIDEConfiguration(Configuration featureIDEConfig) {
        this.featureIDEConfig = featureIDEConfig;
    }

    public FeatureIDEConfiguration(FeatureModelFormula fm, List<String> activeFeatures) {
        this(new Configuration(fm));

        for (String activeFeature : activeFeatures) {
            featureIDEConfig.setManual(activeFeature, Selection.SELECTED);
        }

        // Selection might be incomplete (e.g., parent feature not selected)
        final ConfigurationAnalyzer analyzer = new ConfigurationAnalyzer(new ConfigurationPropagator(fm, featureIDEConfig));
        analyzer.completeMin();
    }

    public FeatureIDEConfiguration(Path p) throws ExtensionManager.NoSuchExtensionException {
        this(ConfigurationFactoryManager.getInstance().getFactory(p, ConfigFormatManager.getDefaultFormat()).create());
    }

    public Map<Object, Boolean> toAssignment() {
        return asAssignment.run();
    }

    @Override
    public boolean satisfies(Node formula) {
        return formula.getValue(toAssignment());
    }
}
