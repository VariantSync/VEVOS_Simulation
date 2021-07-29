package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.SourceCodeFile;

public class SourceCodeFileVisitorFocus extends ArtefactTreeVisitorFocus<SourceCodeFile> {
    public SourceCodeFileVisitorFocus(SourceCodeFile artefact) {
        super(artefact);
    }

    @Override
    public void accept(ArtefactVisitor visitor) {
        visitor.visitSourceCodeFile(this);
    }

    /**
     * Visit the single child of the SourceCodeFile in focus with the given visitor.
     * (Each SourceCodeFile has exactly one LineBasedAnnotation as child.)
     */
    public void visitRootAnnotation(ArtefactVisitor visitor) {
        value.getRootAnnotation().createVisitorFocus().accept(visitor);
    }

    public void skipRootAnnotationButVisitItsSubtrees(final ArtefactVisitor visitor) {
        super.visitTrees(value.getRootAnnotation().getSubtrees(), visitor);
    }
}
