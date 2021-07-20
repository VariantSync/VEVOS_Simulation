package de.variantsync.evolution.variability.pc;

import java.util.function.Consumer;

public interface ArtefactVisitor {
    <T extends ArtefactTree<?>> void visitArtefactTree(ArtefactTree<T> artefact);
    void visitSourceCodeFile(SourceCodeFile artefact);
    void visitLineBasedAnnotation(LineBasedAnnotation artefact);

    static ArtefactVisitor fromConsumer(Consumer<Artefact> c) {
        return new ArtefactVisitor() {
            @Override
            public <T extends ArtefactTree<?>> void visitArtefactTree(ArtefactTree<T> artefact) {
                c.accept(artefact);
            }

            @Override
            public void visitSourceCodeFile(SourceCodeFile artefact) {
                c.accept(artefact);
            }

            @Override
            public void visitLineBasedAnnotation(LineBasedAnnotation artefact) {
                c.accept(artefact);
            }
        };
    }
}
