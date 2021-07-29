package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.variability.pc.LineBasedAnnotation;
import org.prop4j.Node;

public class KernelHavenVariantPCIO extends KernelHavenPCIO {
    public KernelHavenVariantPCIO() {
        super(".variant.csv");
    }

    @Override
    protected LineBasedAnnotation createAnnotation(final Node blockCondition, final int startLine, final int endLine) {
        return new LineBasedAnnotation(blockCondition, startLine, endLine, false);
    }
}
