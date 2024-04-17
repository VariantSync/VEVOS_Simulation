package org.variantsync.vevos.simulation.feature.config;

import de.ovgu.featureide.fm.core.base.IFeature;
import org.prop4j.Node;

import java.util.List;

/**
 * A configuration is an assignment of features to values.
 * This is a total configuration.
 */
public interface IConfiguration {
    /**
     * Evaluate the given propositional formula.
     * @param formula Formula to evaluate.
     * @return True iff this IConfiguration is a satisfying assignment for the given formula.
     */
    boolean satisfies(Node formula);

    /**
     * @return the list of features of this configuration
     */
    List<IFeature> getFeatures();
}
