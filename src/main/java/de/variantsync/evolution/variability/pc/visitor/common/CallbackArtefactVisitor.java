package de.variantsync.evolution.variability.pc.visitor.common;

import de.variantsync.evolution.variability.pc.Artefact;
import de.variantsync.evolution.variability.pc.ArtefactTree;
import de.variantsync.evolution.variability.pc.visitor.*;

import java.util.function.Consumer;

public record CallbackArtefactVisitor(Consumer<Artefact> onEnter, Consumer<Artefact> onLeave) implements ArtefactVisitor {
    private void callback(ArtefactTreeVisitorContext<? extends Artefact> context) {
        onEnter.accept(context.getValue());
        context.visitAllSubtrees(this);
        onLeave.accept(context.getValue());
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(SyntheticArtefactTreeNodeVisitorContext<T> context) {
        callback(context);
    }

    @Override
    public void visitSourceCodeFile(SourceCodeFileVisitorContext context) {
        callback(context);
    }

    @Override
    public void visitLineBasedAnnotation(LineBasedAnnotationVisitorContext context) {
        callback(context);
    }
}
