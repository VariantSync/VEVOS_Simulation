package org.variantsync.vevos.simulation.variability.pc.visitor;

import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;

public class LineBasedAnnotationVisitorFocus extends ArtefactTreeVisitorFocus<LineBasedAnnotation> {
    public LineBasedAnnotationVisitorFocus(final LineBasedAnnotation artefact) {
        super(artefact);
    }

    @Override
    public void accept(final ArtefactVisitor visitor) {
        visitor.visitLineBasedAnnotation(this);
    }
}
