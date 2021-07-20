package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.ArtefactTree;

public interface ArtefactVisitor {
    <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(SyntheticArtefactTreeNodeVisitorContext<T> context);
    void visitSourceCodeFile(SourceCodeFileVisitorContext context);
    void visitLineBasedAnnotation(LineBasedAnnotationVisitorContext context);
}
