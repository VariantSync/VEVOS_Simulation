package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.LineBasedAnnotation;

public class LineBasedAnnotationVisitorContext extends ArtefactTreeVisitorContext<LineBasedAnnotation> {
    public LineBasedAnnotationVisitorContext(LineBasedAnnotation artefact) {
        super(artefact);
    }

    @Override
    public void accept(ArtefactVisitor visitor) {
        visitor.visitLineBasedAnnotation(this);
    }
}
