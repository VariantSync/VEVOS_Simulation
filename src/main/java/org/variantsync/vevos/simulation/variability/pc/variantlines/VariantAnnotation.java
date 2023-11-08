package org.variantsync.vevos.simulation.variability.pc.variantlines;

import org.prop4j.Node;
import org.variantsync.vevos.simulation.util.fide.FormulaUtils;
import org.variantsync.vevos.simulation.util.fide.bugfix.FixTrueFalse;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;

import java.util.ArrayList;
import java.util.List;

public record VariantAnnotation(
        Node condition,
        List<VariantLineChunk> lines
) implements VariantLineChunk {
    @Override
    public List<String> project(final VariantGenerationOptions projectionOptions, final List<String> splFileLines) {
        final List<String> result = new ArrayList<>();

        for (final VariantLineChunk child : lines) {
            result.addAll(child.project(projectionOptions, splFileLines));
        }

        return result;
    }

    public boolean isTrue() {
        return FixTrueFalse.isTrue(condition);
    }
}
