package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.ArtefactTree;

/**
 * Abstract focus for subclasses of ArtefactTree. Offers methods for visiting subtrees.
 * @param <A> A subtype of ArtefactTree this focus should be specialized to.
 */
public abstract class ArtefactTreeVisitorFocus<A extends ArtefactTree<?>> extends ArtefactVisitorFocus<A> {
    public ArtefactTreeVisitorFocus(A artefact) {
        super(artefact);
    }

    /**
     * Visits the subtree at the given index with the given visitor.
     */
    public void visitSubtree(int index, ArtefactVisitor visitor) {
        value.getSubtrees().get(index).createVisitorFocus().accept(visitor);
    }

    /**
     * Visits the given subtree with the given visitor.
     * The caller is responsible for ensuring that the given subtree is indeed a subtree of the value of this focus
     * (@see ArtefactVisitorFocus::getValue).
     */
    public void visitSubtree(ArtefactTree<?> subtree, ArtefactVisitor visitor) {
        // We could check here that subtree is indeed a child of value.
        subtree.createVisitorFocus().accept(visitor);
    }

    /**
     * @return The amount of subtrees the tree in focus has.
     */
    public int getNumberOfSubtrees() {
        return value.getSubtrees().size();
    }

    /**
     * Visits all subtrees recursively with the given visitor.
     * The trees will be visited in the order that is given by the tree in focus.
     */
    public void visitAllSubtrees(ArtefactVisitor visitor) {
        for (ArtefactTree<?> subtree : value.getSubtrees()) {
            visitSubtree(subtree, visitor);
        }
    }
}
