package de.variantsync.evolution.feature;

import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.variantsync.evolution.util.fide.ConfigurationUtils;
import org.prop4j.Node;

import java.util.Arrays;
import java.util.Map;

public class Variant {
    private final String name;
    private final Configuration configuration;
    private final Map<Object, Boolean> configurationAsAssignment;

    public Variant(String name, Configuration configuration) {
        this.name = name;
        this.configuration = configuration;
        this.configurationAsAssignment = ConfigurationUtils.ToAssignment(configuration);
    }

    public boolean isImplementing(final Node presenceCondition) {
        return ConfigurationUtils.IsSatisfyingAssignment(configurationAsAssignment, presenceCondition);
    }

    public String getName() {
        return name;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return "Variant " + name + " with configuration "  +
                Arrays.stream(configuration.toString().split("\\n")).reduce((a, b) -> a + ", " + b);
    }
}
