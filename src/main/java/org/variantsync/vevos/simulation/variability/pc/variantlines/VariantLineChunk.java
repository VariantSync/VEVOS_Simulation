package org.variantsync.vevos.simulation.variability.pc.variantlines;

import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;

import java.util.List;

public interface VariantLineChunk {
    List<String> project(final VariantGenerationOptions projectionOptions, final List<String> splFileLines);
}
