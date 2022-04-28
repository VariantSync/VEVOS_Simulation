package org.variantsync.vevos.simulation.variability.pc.variantlines;

import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;

import java.util.List;

public record VariantLine(Integer lineNumber) implements VariantLineChunk {
    @Override
    public List<String> project(final VariantGenerationOptions projectionOptions, final List<String> splFileLines) {
        return List.of(splFileLines.get(lineNumber - 1));
    }
}
