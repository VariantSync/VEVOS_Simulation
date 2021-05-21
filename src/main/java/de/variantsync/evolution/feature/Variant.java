package de.variantsync.evolution.feature;

import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.variantsync.evolution.util.fide.ConfigurationUtils;
import org.prop4j.Node;

public record Variant(String name, Configuration configuration) {
    public boolean isImplementing(final Node presenceCondition) {
        return ConfigurationUtils.IsSatisfyingAssignment(configuration, presenceCondition);
    }
}
