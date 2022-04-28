package org.variantsync.vevos.simulation.variability.pc.variantlines;

import org.variantsync.vevos.simulation.util.Logger;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;

import java.util.List;

public record VariantLine(Integer lineNumber) implements VariantLineChunk {
    @Override
    public List<String> project(final VariantGenerationOptions projectionOptions, final List<String> splFileLines) {
        // The list splFileLines is 0-based.
        // Our lineNumber is 1-based because line numbers are typically given 1-based.
        final int sourceLineNo = lineNumber - 1;

        if (sourceLineNo >= splFileLines.size()) {
            final String logMessage = "Skipped copying line "
                    + lineNumber
                    + " as it is out of bounds [1, "
                    + splFileLines.size()
                    + "]!";

            if (sourceLineNo > splFileLines.size()) {
                // This was logged frequently and is caused by https://bugs.openjdk.java.net/browse/JDK-8199413
                // Skipping the line really is the best solution, as the empty line is created by appending a line separator
                // to the previous line. I added the additional if-statement, to only catch cases in which more than one line
                // is out of bounds, which indicates a serious problem.
                Logger.error(logMessage);
            }

            return List.of();
        } else {
            return List.of(splFileLines.get(sourceLineNo));
        }
    }
}
