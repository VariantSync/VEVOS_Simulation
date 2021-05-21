package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import org.prop4j.Node;

import java.nio.file.Path;

/**
 * A mapping of artefacts to features.
 */
public interface Artefact {
    /**
     * @return the feature mapping of this node (i.e., the feature formula associated to this element).
     */
    Node getFeatureMapping();

    /**
     * @return the presence condition of this node
     *         (i.e., the conjunction of feature mapping with the parents presence condition).
     */
    Node getPresenceCondition();

    Path getFile();

    /**
     * Projects this feature trace to a specific variant and returns the projection.
     * This object will not be altered, meaning a copy that represents the derived variant is returned.
     * @param variant The variant for which the feature traces should be reduced.
     */
    Result<Unit, Exception> project(Variant variant, Path sourceDir, Path targetDir);

//    Result<Unit, Exception> write(Path sourceDir, Path targetDir);

    default String prettyPrint() {
        return prettyPrint("");
    }

    String prettyPrint(String indent);
}
