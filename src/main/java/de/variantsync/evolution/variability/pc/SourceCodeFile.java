package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a variable source code file (e.g., because part of a plugin or only conditionally included).
 */
public class SourceCodeFile extends Annotated {
    private final Path relativePath;

    public SourceCodeFile(Path relativePath, Node featureMapping) {
        super(featureMapping);
        this.relativePath = relativePath;
    }

    public Path getRelativePath() {
        return relativePath;
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
        return relativePath.equals(that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), relativePath);
    }
}
