package de.variantsync.evolution.variability.pc.groundtruth;

import de.variantsync.evolution.variability.pc.LineBasedAnnotation;

public record AnnotationGroundTruth(
        LineBasedAnnotation splArtefact,
        LineBasedAnnotation variantArtefact,
        BlockMatching matching
)
{
}
