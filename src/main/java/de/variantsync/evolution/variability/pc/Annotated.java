package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class Annotated {
    private Annotated parent;
    private final List<PreprocessorBlock> blocks = new ArrayList<>();

    public abstract Node getFeatureMapping();
    public abstract Node getPresenceCondition();

    void setParent(Annotated parent) {
        this.parent = parent;
    }
    public Annotated getParent() {
        return parent;
    }

    public void addBlock(PreprocessorBlock b) {
        blocks.add(b);
        b.setParent(this);
    }
}
