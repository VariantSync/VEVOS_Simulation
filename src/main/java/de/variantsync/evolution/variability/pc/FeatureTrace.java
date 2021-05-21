package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import org.prop4j.Node;

/**
 * A mapping of source code entities to features.
 */
public interface FeatureTrace {
    /**
     * @return the feature mapping of this node (i.e., the feature formula associated to this element).
     */
    Node getFeatureMapping();

    /**
     * @return the presence condition of this node
     *         (i.e., the conjunction of feature mapping with the parents presence condition).
     */
    Node getPresenceCondition();

    /**
     * Projects this feature trace to a specific variant and returns the projection.
     * This object will not be altered, meaning a copy that represents the derived variant is returned.
     * @param variant The variant for which the feature traces should be reduced.
     */
    FeatureTrace project(Variant variant);

    default String prettyPrint() {
        return prettyPrint("");
    }

    String prettyPrint(String indent);
}
