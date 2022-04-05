package org.variantsync.vevos.simulation.variability.pc.groundtruth;

import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;

public record AnnotationGroundTruth(
        LineBasedAnnotation splArtefact,
        LineBasedAnnotation variantArtefact,
        BlockMatching matching
)
{
}
