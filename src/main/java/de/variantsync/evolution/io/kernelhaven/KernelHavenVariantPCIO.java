package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.variability.pc.LineBasedAnnotation;
import org.prop4j.Node;

public class KernelHavenVariantPCIO extends KernelHavenPCIO {
    public KernelHavenVariantPCIO() {
        super(".variant.csv");
    }

    @Override
    protected LineBasedAnnotation createAnnotation(Node blockCondition, int startLine, int endLine) {
        return new LineBasedAnnotation(blockCondition, startLine, endLine, false);
    }
}
