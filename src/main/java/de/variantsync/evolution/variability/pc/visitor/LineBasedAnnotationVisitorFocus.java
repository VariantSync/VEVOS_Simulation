package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.LineBasedAnnotation;

public class LineBasedAnnotationVisitorFocus extends ArtefactTreeVisitorFocus<LineBasedAnnotation> {
    public LineBasedAnnotationVisitorFocus(LineBasedAnnotation artefact) {
        super(artefact);
    }

    @Override
    public void accept(ArtefactVisitor visitor) {
        visitor.visitLineBasedAnnotation(this);
    }
}
