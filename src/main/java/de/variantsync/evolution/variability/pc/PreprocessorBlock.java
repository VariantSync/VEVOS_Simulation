package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.util.fide.FormulaUtils;
import org.prop4j.Node;

public class PreprocessorBlock extends Annotated {
    private final Node featureMapping;
    private final int lineFrom;
    private final int lineTo;

    public PreprocessorBlock(Node featureMapping, int lineFrom, int lineTo) {
        this.featureMapping = featureMapping;
        this.lineFrom = lineFrom;
        this.lineTo = lineTo;
    }

    public Node getFeatureMapping() {
        return featureMapping;
    }

    public Node getPresenceCondition() {
        final Annotated parent = getParent();
        return parent == null ?
                featureMapping :
                FormulaUtils.AndSimplified(parent.getPresenceCondition(), featureMapping);
    }

    public int getLineFrom() {
        return lineFrom;
    }

    public int getLineTo() {
        return lineTo;
    }

    @Override
    public String toString() {
        return featureMapping +
                " from " + lineFrom +
                " to " + lineTo;
    }
}
