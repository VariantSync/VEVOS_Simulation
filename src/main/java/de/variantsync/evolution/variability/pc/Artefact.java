package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.visitor.ArtefactVisitor;
import de.variantsync.evolution.variability.pc.visitor.ArtefactVisitorContext;
import de.variantsync.evolution.variability.pc.visitor.common.PrettyPrinter;
import org.prop4j.Node;

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

    /**
     * @return File on disk this artefact is associated to. This artefact can represent the entire file or a part of
     * that file.
     */
    CaseSensitivePath getFile();

    void simplify();

    /**
     * Projects this feature trace to a specific variant and returns the projection.
     * This object will not be altered, meaning a copy that represents the derived variant is returned.
     * @param variant The variant for which the feature traces should be reduced.
     * @param sourceDir The directory of the product line from which variants should be build.
     * @param targetDir Output directory the variant will be generated into.
     */
    Result<? extends Artefact, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir);

    default void accept(ArtefactVisitor visitor) {
        createVisitorContext().accept(visitor);
    }

    ArtefactVisitorContext<? extends Artefact> createVisitorContext();

    default String prettyPrint() {
        return new PrettyPrinter().prettyPrint(this);
    }
}
