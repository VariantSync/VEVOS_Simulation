package org.variantsync.vevos.simulation.io.kernelhaven;

import org.prop4j.Node;
import org.variantsync.vevos.simulation.variability.pc.AnnotationStyle;
import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;

/**
 * IO for presence condition of source code of variants of a software product lines.
 * The annotated code will be considered to be annotated externally (i.e., not inline), so there are no CPP annotations.
 */
public class KernelHavenVariantPCIO extends KernelHavenPCIO {
    public KernelHavenVariantPCIO() {
        super(".variant.csv");
    }

    @Override
    protected LineBasedAnnotation createAnnotation(final Node blockCondition, final int startLine, final int endLine) {
        return new LineBasedAnnotation(blockCondition, startLine, endLine, AnnotationStyle.External);
    }
}
