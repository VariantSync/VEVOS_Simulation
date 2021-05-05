package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.nio.file.Path;
import java.util.List;

public record SourceCodeFile(Path relativePath, Node presenceCondition, List<PPBlock> blocks) {
    void addBlock(PPBlock b) {
        blocks.add(b);
    }
}
