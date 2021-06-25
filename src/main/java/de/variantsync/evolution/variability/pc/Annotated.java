package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.util.CaseSensitivePath;
import org.prop4j.Node;

import java.nio.file.Path;

/**
 * Represents an artefact that can be annotated with FeatureAnnotations (i.e., line-based feature annotations).
 * In particular, FeatureAnnotations themselves derive Annotated because annotations might be nested:
 *
 * #if A
 *   #if B
 *   ...
 *   #endif
 * #endif
 */
public abstract class Annotated extends ArtefactTree<LineBasedAnnotation> {
    protected Annotated(Node featureMapping) {
        super(featureMapping);
    }

    protected Annotated(Node featureMapping, CaseSensitivePath file) {
        super(featureMapping, file);
    }
}
