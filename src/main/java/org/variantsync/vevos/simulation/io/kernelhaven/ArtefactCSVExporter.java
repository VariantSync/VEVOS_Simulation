package org.variantsync.vevos.simulation.io.kernelhaven;

import org.prop4j.NodeWriter;
import org.variantsync.vevos.simulation.io.data.CSV;
import org.variantsync.vevos.simulation.util.fide.FormulaUtils;
import org.variantsync.vevos.simulation.variability.pc.ArtefactTree;
import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;
import org.variantsync.vevos.simulation.variability.pc.SourceCodeFile;
import org.variantsync.vevos.simulation.variability.pc.visitor.ArtefactVisitor;
import org.variantsync.vevos.simulation.variability.pc.visitor.LineBasedAnnotationVisitorFocus;
import org.variantsync.vevos.simulation.variability.pc.visitor.SourceCodeFileVisitorFocus;
import org.variantsync.vevos.simulation.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;

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
     * @return The CSV row.
     */
    private String[] toRow(final LineBasedAnnotation annotation) {
        final String[] row = makeRow();
        row[0] = currentFile.getFile().toString();
        row[1] = FormulaUtils.toFormulaString(currentFile.getPresenceCondition(), NodeWriter.javaSymbols);
        row[2] = FormulaUtils.toFormulaString(annotation.getFeatureMapping(), NodeWriter.javaSymbols);
        row[3] = FormulaUtils.toFormulaString(annotation.getPresenceCondition(), NodeWriter.javaSymbols);
        row[4] = "" + annotation.getLineFrom();
        // -1 because Kernelhaven stores annotations as [#if, #endif) intervals, so we have to point one line before the annotation end (#endif).
        row[5] = "" + (annotation.getLineTo() - (annotation.isMacro() ? 1 : 0));
        return row;
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(final SyntheticArtefactTreeNodeVisitorFocus<T> focus) {
        focus.visitAllSubtrees(this);
    }

    @Override
    public void visitSourceCodeFile(final SourceCodeFileVisitorFocus focus) {
        currentFile = focus.getValue();
        focus.skipRootAnnotationButVisitItsSubtrees(this);
        currentFile = null;
    }

    @Override
    public void visitLineBasedAnnotation(final LineBasedAnnotationVisitorFocus focus) {
        final LineBasedAnnotation annotation = focus.getValue();
        csv.add(toRow(annotation));
        focus.visitAllSubtrees(this);
    }
}