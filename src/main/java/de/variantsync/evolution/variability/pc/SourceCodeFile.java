package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
}
