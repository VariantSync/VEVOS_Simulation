package org.variantsync.vevos.simulation.io.kernelhaven;

import org.prop4j.Node;
import org.variantsync.vevos.simulation.variability.pc.AnnotationStyle;
import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.LineType;

/**
 * IO for presence condition of source code of software product lines.
 * The annotated code will be considered to have inline annotations (in CPP style):
 * #if
 * foo();
 * #endif
 * This importer specifically expects the imported files to be produced by KernelHaven to adjust for its quirks.
 */
public class KernelHavenSPLPCIO extends KernelHavenPCIO {
    public KernelHavenSPLPCIO() {
        super(".spl.csv");
    }

    @Override
    protected LineBasedAnnotation createAnnotation(final Node blockCondition, final Node presenceCondition,
                                                   final LineType lineType, final int startLine, int endLine) {
        /// If a block starts at 1 in KernelHaven files, it does not denote an #if but the entire file.
        /// Thus, there is no #if at line 1 but LineBasedAnnotation expects a macro at startLine.
        final boolean isVirtualSurroundingTrue = startLine == 1;
        /*
        Line numbers for the all surrounding true are given in [1, last line of file] format.
        Line numbers for macros are given in [firstCodeLine, lastCodeLine] format.
         */

        return new LineBasedAnnotation(
                blockCondition,
                presenceCondition,
                lineType,
                startLine,
                endLine,
                isVirtualSurroundingTrue ? AnnotationStyle.External : AnnotationStyle.Internal);
    }
}
