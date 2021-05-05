package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.util.fide.FormulaUtils;
import org.prop4j.Node;

public record PPBlock(SourceCodeFile parent, Node featureMapping, int lineFrom, int lineTo) {
    public Node getPresenceCondition() {
        return FormulaUtils.AndSimplified(parent.presenceCondition(), featureMapping);
    }
}
