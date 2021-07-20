package de.variantsync.evolution.variability.pc.visitor.common;

import de.variantsync.evolution.variability.pc.Artefact;
import de.variantsync.evolution.variability.pc.ArtefactTree;
import de.variantsync.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.evolution.variability.pc.SourceCodeFile;
import de.variantsync.evolution.variability.pc.visitor.*;

public class PrettyPrinter implements ArtefactVisitor {
    private StringBuilder builder;
    private String indent = "";

    public String prettyPrint(Artefact artefact) {
        builder = new StringBuilder();
        artefact.accept(this);
        return builder.toString();
    }

    private void printSubtrees(ArtefactTreeVisitorContext<? extends Artefact> context) {
        final String oldIndent = indent;
        indent += "  ";
        context.visitAllSubtrees(this);
        indent = oldIndent;
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(SyntheticArtefactTreeNodeVisitorContext<T> context) {
        builder.append(indent).append("[").append(System.lineSeparator());
        printSubtrees(context);
        builder.append(indent).append("]").append(System.lineSeparator());
    }

    @Override
    public void visitSourceCodeFile(SourceCodeFileVisitorContext context) {
        final SourceCodeFile file = context.getValue();
        builder
                .append(indent)
                .append(file.getFile())
                .append("<")
                .append(file.getFeatureMapping())
                .append(">[")
                .append(System.lineSeparator());

        printSubtrees(context);

        builder.append(indent).append("]").append(System.lineSeparator());
    }

    @Override
    public void visitLineBasedAnnotation(LineBasedAnnotationVisitorContext context) {
        final LineBasedAnnotation annotation = context.getValue();
        builder
                .append(indent)
                .append("#if ")
                .append(annotation.getFeatureMapping())
                .append(" @")
                .append(annotation.getLineFrom())
                .append(System.lineSeparator());

        printSubtrees(context);

        builder.append(indent).append("#endif @").append(annotation.getLineTo()).append(System.lineSeparator());
    }
}
