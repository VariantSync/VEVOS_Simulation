package org.variantsync.vevos.simulation.feature.config;

import de.ovgu.featureide.fm.core.base.IFeature;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Impossible configuration intended to be used for debugging only.
 * This configuration is a satisfying assignment for all propositional formulas.
 */
public class SayYesToAllConfiguration implements IConfiguration {
    @Override
    public boolean satisfies(final Node formula) {
        return true;
    }

    @Override
    public List<IFeature> getFeatures() {
        return new ArrayList<>();
    }
}
