package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SourceCodeFile extends Annotated {
    private final Path relativePath;
    private final Node presenceCondition;

    public SourceCodeFile(Path relativePath, Node presenceCondition) {
        this.relativePath = relativePath;
        this.presenceCondition = presenceCondition;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    @Override
    public Node getFeatureMapping() {
        return presenceCondition;
    }

    @Override
    public Node getPresenceCondition() {
        return presenceCondition;
    }

    @Override
    protected void prettyPrintHeader(String indent, StringBuilder builder) {
        builder
                .append(indent)
                .append(relativePath)
                .append("<")
                .append(getFeatureMapping())
                .append(">[");
    }

    @Override
    protected void prettyPrintFooter(String indent, StringBuilder builder) {
        builder.append(indent).append("]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SourceCodeFile that = (SourceCodeFile) o;
        return relativePath.equals(that.relativePath) && presenceCondition.equals(that.presenceCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), relativePath, presenceCondition);
    }
}
