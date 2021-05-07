package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.util.fide.FormulaUtils;
import org.prop4j.Node;

public class PreprocessorBlock {
    private final Node featureMapping;
    private final int lineFrom;
    private final int lineTo;
    private SourceCodeFile parent;

    public PreprocessorBlock(Node featureMapping, int lineFrom, int lineTo) {
        this.featureMapping = featureMapping;
        this.lineFrom = lineFrom;
        this.lineTo = lineTo;
    }

    void setParent(SourceCodeFile parent) {
        this.parent = parent;
    }

    public Node getFeatureMapping() {
        return featureMapping;
    }

    public Node getPresenceCondition() {
        return parent == null ?
                featureMapping :
                FormulaUtils.AndSimplified(parent.presenceCondition(), featureMapping);
    }

    public int getLineFrom() {
        return lineFrom;
    }

    public int getLineTo() {
        return lineTo;
    }

    public SourceCodeFile getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return featureMapping +
                " from " + lineFrom +
                " to " + lineTo;
    }
}
