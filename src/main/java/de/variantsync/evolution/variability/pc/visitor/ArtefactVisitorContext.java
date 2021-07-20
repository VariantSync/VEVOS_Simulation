package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.Artefact;

public abstract class ArtefactVisitorContext<A extends Artefact> {
    protected final A value;

    public ArtefactVisitorContext(A artefact) {
        this.value = artefact;
    }

    public A getValue() {
        return value;
    }

    public abstract void accept(ArtefactVisitor visitor);
}
