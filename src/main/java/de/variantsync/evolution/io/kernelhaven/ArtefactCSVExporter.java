package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.util.fide.FormulaUtils;
import de.variantsync.evolution.variability.pc.ArtefactTree;
import de.variantsync.evolution.variability.pc.visitor.ArtefactVisitor;
import de.variantsync.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.evolution.variability.pc.SourceCodeFile;
import de.variantsync.evolution.variability.pc.visitor.LineBasedAnnotationVisitorFocus;
import de.variantsync.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import de.variantsync.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;
import org.prop4j.NodeWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Exporter for artefact trees to CSV files in the format from KernelHaven.
 *
 * CSV Header:
 * Path;File Condition;Block Condition;Presence Condition;start;end
 */
public class ArtefactCSVExporter implements ArtefactVisitor {
    private final static int COLUMN_COUNT = 6;
    private final List<String[]> csv = new ArrayList<>();
    private SourceCodeFile currentFile = null;

    /**
     * @return A new csv row.
     */
    private static String[] makeRow() {
        return new String[COLUMN_COUNT];
    }

    public ArtefactCSVExporter() {
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

    /**
     * Finalizes the export.
     * @return CSV object that can be used for writing to disk.
     */
    public CSV export() {
        return new CSV(csv);
    }

    /**
     * Creates a CSV row for the given annotation but uses the given start and end lines.
     * @param annotation The annotation to create a row for.
     * @param start The first line in which this annotation is at least valid.
     * @param end The last line in which this annotation is at least valid. All lines between [start, end] will be
     *            considered annotated with the given annotation.
     * @return The CSV row.
     */
    private String[] toRow(LineBasedAnnotation annotation, int start, int end) {
        final String[] row = makeRow();
        row[0] = currentFile.getFile().toString();
        row[1] = FormulaUtils.toString(currentFile.getPresenceCondition(), NodeWriter.javaSymbols);
        row[2] = FormulaUtils.toString(annotation.getFeatureMapping(), NodeWriter.javaSymbols);
        row[3] = FormulaUtils.toString(annotation.getPresenceCondition(), NodeWriter.javaSymbols);
        row[4] = "" + start;
        row[5] = "" + end;
        return row;
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(SyntheticArtefactTreeNodeVisitorFocus<T> focus) {
        focus.visitAllSubtrees(this);
    }

    @Override
    public void visitSourceCodeFile(SourceCodeFileVisitorFocus focus) {
        currentFile = focus.getValue();
        focus.skipRootAnnotationButVisitItsSubtrees(this);
        currentFile = null;
    }

    @Override
    public void visitLineBasedAnnotation(LineBasedAnnotationVisitorFocus focus) {
        final LineBasedAnnotation annotation = focus.getValue();
        csv.add(toRow(annotation, annotation.getLineFrom(), annotation.getLineTo()));
        focus.visitAllSubtrees(this);

        /*
        Check if there are lines that are annotated by the current annotation but not a nested application.
        Such lines may occur before, between, or after nested annotations. For example, in the following code
        fragment, all lines marked with (*) have to be treated manually.

        # if A
        (*)
          #if B
          #endif
        (*)
          #if C
          #endif
        (*)
        #endif

        Thus, check if there are some lines between the current annotation start and the inner annotations.
        */
//        int currentLine = annotation.getLineFrom();
//        for (LineBasedAnnotation subtree : annotation.getSubtrees()) {
//            if (currentLine < subtree.getLineFrom()) {
//                csv.add(toRow(annotation, currentLine, subtree.getLineFrom() - 1));
//            }
//            focus.visitSubtree(subtree, this);
//            currentLine = subtree.getLineTo() + 1;
//        }
//
//        /// If there were no children or there are lines annotated after the last nested annotation (see example above)
//        /// export these lines.
//        if (currentLine <= annotation.getLineTo()) {
//            csv.add(toRow(annotation, currentLine, annotation.getLineTo()));
//        }
    }
}