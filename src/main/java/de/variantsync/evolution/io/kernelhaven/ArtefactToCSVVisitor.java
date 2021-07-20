package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.variability.pc.ArtefactTree;
import de.variantsync.evolution.variability.pc.visitor.ArtefactVisitor;
import de.variantsync.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.evolution.variability.pc.SourceCodeFile;
import de.variantsync.evolution.variability.pc.visitor.LineBasedAnnotationVisitorContext;
import de.variantsync.evolution.variability.pc.visitor.SourceCodeFileVisitorContext;
import de.variantsync.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorContext;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV Header:
 * Path;File Condition;Block Condition;Presence Condition;start;end
 */
public class ArtefactToCSVVisitor  implements ArtefactVisitor {
    private final static int ColumnCount = 6;
    private final List<String[]> csv = new ArrayList<>();
    private SourceCodeFile currentFile = null;

    private static String[] makeRow() {
        return new String[ColumnCount];
    }

    public ArtefactToCSVVisitor() {
        // create header
        final String[] header = makeRow();
        header[0] = "Path";
        header[1] = "File Condition";
        header[2] = "Block Condition";
        header[3] = "Presence Condition";
        header[4] = "start";
        header[5] = "end";
        csv.add(header);
    }

    public CSV export() {
        return new CSV(csv);
    }

    private String[] toRow(LineBasedAnnotation artefact, int start, int end) {
        final String[] row = makeRow();
        row[0] = currentFile.getFile().toString();
        row[1] = currentFile.getPresenceCondition().toString();
        row[2] = artefact.getFeatureMapping().toString();
        row[3] = artefact.getPresenceCondition().toString();
        row[4] = "" + start;
        row[5] = "" + end;
        return row;
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(SyntheticArtefactTreeNodeVisitorContext<T> context) {
        context.visitAllSubtrees(this);
    }

    @Override
    public void visitSourceCodeFile(SourceCodeFileVisitorContext context) {
        currentFile = context.getValue();
        context.visitRootAnnotation(this);
        currentFile = null;
    }

    @Override
    public void visitLineBasedAnnotation(LineBasedAnnotationVisitorContext context) {
        final LineBasedAnnotation annotation = context.getValue();

        int currentLine = annotation.getLineFrom();
        for (LineBasedAnnotation subtree : annotation.getSubtrees()) {
            if (currentLine < subtree.getLineFrom()) {
                csv.add(toRow(annotation, currentLine, subtree.getLineFrom() - 1));
            }
            context.visitSubtree(subtree, this);
            currentLine = subtree.getLineTo() + 1;
        }

        if (currentLine <= annotation.getLineTo()) {
            csv.add(toRow(annotation, currentLine, annotation.getLineTo()));
        }
    }
}