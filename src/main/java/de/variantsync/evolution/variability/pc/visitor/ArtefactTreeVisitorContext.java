package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.ArtefactTree;

public abstract class ArtefactTreeVisitorContext<A extends ArtefactTree<?>> extends ArtefactVisitorContext<A> {
    public ArtefactTreeVisitorContext(A artefact) {
        super(artefact);
    }

    public void visitSubtree(int index, ArtefactVisitor visitor) {
        value.getSubtrees().get(index).createVisitorContext().accept(visitor);
    }

    public void visitSubtree(ArtefactTree<?> subtree, ArtefactVisitor visitor) {
        // We could check here that subtree is indeed a child of value.
        subtree.createVisitorContext().accept(visitor);
    }

    public int getNumberOfSubtrees() {
        return value.getSubtrees().size();
    }

    public void visitAllSubtrees(ArtefactVisitor visitor) {
        for (ArtefactTree<?> subtree : value.getSubtrees()) {
            visitSubtree(subtree, visitor);
        }
    }
}
