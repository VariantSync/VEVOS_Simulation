package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.LineBasedAnnotation;

public class LineBasedAnnotationVisitorFocus extends ArtefactTreeVisitorFocus<LineBasedAnnotation> {
    public LineBasedAnnotationVisitorFocus(final LineBasedAnnotation artefact) {
        super(artefact);
    }

    @Override
    public void accept(final ArtefactVisitor visitor) {
        visitor.visitLineBasedAnnotation(this);
    }
}
