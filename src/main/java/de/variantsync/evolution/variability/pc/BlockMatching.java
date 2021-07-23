package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.util.functional.Monoid;

import java.util.*;

public class BlockMatching implements Monoid<BlockMatching> {
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

    public static BlockMatching mEmpty() {
        return new BlockMatching();
    }

    @Override
    public BlockMatching mAppend(BlockMatching other) {
        final BlockMatching result = mEmpty();
        result.splToVariant.putAll(other.splToVariant);
        result.variantToSPL.putAll(other.variantToSPL);
        return result;
    }
}