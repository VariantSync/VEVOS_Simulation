package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.evolution.variability.pc.SourceCodeFile;

public class SourceCodeFileVisitorContext extends ArtefactTreeVisitorContext<SourceCodeFile> {
    public SourceCodeFileVisitorContext(SourceCodeFile artefact) {
        super(artefact);
    }

    @Override
    public void accept(ArtefactVisitor visitor) {
        visitor.visitSourceCodeFile(this);
    }

    public void visitRootAnnotation(ArtefactVisitor visitor) {
        value.getRootAnnotation().createVisitorContext().accept(visitor);
    }
}
