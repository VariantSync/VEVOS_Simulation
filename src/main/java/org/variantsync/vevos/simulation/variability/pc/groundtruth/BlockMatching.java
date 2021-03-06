package org.variantsync.vevos.simulation.variability.pc.groundtruth;

import org.variantsync.functjonal.category.Monoid;
import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;

import java.util.HashMap;
import java.util.Map;

public class BlockMatching {
    public static final Monoid<BlockMatching> MONOID = Monoid.From(
            BlockMatching::new,
            (a, b) -> {
                final BlockMatching result = new BlockMatching();
                result.splToVariant.putAll(a.splToVariant);
                result.splToVariant.putAll(b.splToVariant);
                result.variantToSPL.putAll(a.variantToSPL);
                result.variantToSPL.putAll(b.variantToSPL);
                return result;
            }
    );

    private final Map<LineBasedAnnotation, LineBasedAnnotation> splToVariant;
    private final Map<LineBasedAnnotation, LineBasedAnnotation> variantToSPL;

    public BlockMatching() {
        this.splToVariant = new HashMap<>();
        this.variantToSPL = new HashMap<>();
    }

    public void put(final LineBasedAnnotation splAnnotation, final LineBasedAnnotation variantAnnotation) {
        splToVariant.put(splAnnotation, variantAnnotation);
        variantToSPL.put(variantAnnotation, splAnnotation);
    }

    public LineBasedAnnotation getVariantAnnotationOf(final LineBasedAnnotation splAnnotation) {
        return splToVariant.get(splAnnotation);
    }

    public LineBasedAnnotation getSPLAnnotationOf(final LineBasedAnnotation variantAnnotation) {
        return variantToSPL.get(variantAnnotation);
    }

    public boolean isPresentInVariant(final LineBasedAnnotation splAnnotation) {
        return splToVariant.containsKey(splAnnotation);
    }
}