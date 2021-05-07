package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.nio.file.Path;
import java.util.List;

public record SourceCodeFile(Path relativePath, Node presenceCondition, List<PreprocessorBlock> blocks) {
    public void addBlock(PreprocessorBlock b) {
        blocks.add(b);
        b.setParent(this);
    }
}
