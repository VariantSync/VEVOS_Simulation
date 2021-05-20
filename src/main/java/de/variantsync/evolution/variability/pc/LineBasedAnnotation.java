package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;
import java.util.Objects;

/**
 * A line-based annotation of source code, such as preprocessor annotations (#ifdef)
 */
public class LineBasedAnnotation extends Annotated {
    private final int lineFrom;
    private final int lineTo;

    /**
     * Creates a new annotations starting at lineFrom (inclusive @Alex?) and ending at lineTo (inclusive@Alex?).
     * TODO: @Alex: Indexing is zero based?
     */
    public LineBasedAnnotation(Node featureMapping, int lineFrom, int lineTo) {
        super(featureMapping);
        this.lineFrom = lineFrom;
        this.lineTo = lineTo;
    }

    public int getLineFrom() {
        return lineFrom;
    }
    public int getLineTo() {
        return lineTo;
    }

    @Override
    protected LineBasedAnnotation plainCopy() {
        return new LineBasedAnnotation(getFeatureMapping().clone(), lineFrom, lineTo);
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "featureMapping=" + getFeatureMapping() +
                ", from " + lineFrom +
                " to " + lineTo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LineBasedAnnotation that = (LineBasedAnnotation) o;
        return lineFrom == that.lineFrom && lineTo == that.lineTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lineFrom, lineTo);
    }

    @Override
    protected void prettyPrintHeader(String indent, StringBuilder builder) {
        builder.append(indent).append("#if ").append(getFeatureMapping()).append(" @").append(getLineFrom());
    }

    @Override
    protected void prettyPrintFooter(String indent, StringBuilder builder) {
        builder.append(indent).append("#endif @").append(getLineTo());
    }
}
