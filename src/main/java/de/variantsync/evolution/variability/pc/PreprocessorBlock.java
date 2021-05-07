package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.util.fide.FormulaUtils;
import org.prop4j.Node;

import java.util.Objects;

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

    @Override
    protected void prettyPrintHeader(String indent, StringBuilder builder) {
        builder.append(indent).append("#if ").append(getFeatureMapping()).append(" @").append(getLineFrom());
    }

    @Override
    protected void prettyPrintFooter(String indent, StringBuilder builder) {
        builder.append(indent).append("#endif @").append(getLineTo());
    }

    public int getLineFrom() {
        return lineFrom;
    }
    public int getLineTo() {
        return lineTo;
    }

    @Override
    public String toString() {
        return "PreprocessorBlock{" +
                "featureMapping=" + featureMapping +
                ", from " + lineFrom +
                " to " + lineTo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PreprocessorBlock that = (PreprocessorBlock) o;
        return lineFrom == that.lineFrom && lineTo == that.lineTo && featureMapping.equals(that.featureMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), featureMapping, lineFrom, lineTo);
    }
}
