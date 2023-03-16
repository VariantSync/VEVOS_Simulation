package org.variantsync.vevos.simulation.io.kernelhaven;

import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.variantsync.functjonal.Result;
import org.variantsync.functjonal.Unit;
import org.variantsync.functjonal.list.ListHeadTailView;
import org.variantsync.vevos.simulation.io.ResourceLoader;
import org.variantsync.vevos.simulation.io.ResourceWriter;
import org.variantsync.vevos.simulation.io.Resources;
import org.variantsync.vevos.simulation.io.data.CSV;
import org.tinylog.Logger;
import org.variantsync.vevos.simulation.util.fide.bugfix.FixTrueFalse;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.util.io.PathUtils;
import org.variantsync.vevos.simulation.variability.pc.Artefact;
import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;
import org.variantsync.vevos.simulation.variability.pc.SourceCodeFile;
import org.variantsync.vevos.simulation.variability.pc.SyntheticArtefactTreeNode;

import java.nio.file.Path;
import java.util.*;

/**
 * Abstract class to load presence conditions from the csv file generated by KernelHaven.
 */
public abstract class KernelHavenPCIO implements ResourceLoader<Artefact>, ResourceWriter<Artefact> {
    private final String extension;

    protected KernelHavenPCIO(final String extension) {
        this.extension = extension;
    }

    @Override
    public boolean canLoad(final Path p) {
        return PathUtils.hasExtension(p, extension);
    }

    @Override
    public boolean canWrite(final Path p) {
        return canLoad(p);
    }

    @Override
    public Result<Artefact, Exception> load(final Path csvPath) {
        final Map<CaseSensitivePath, SourceCodeFile> files = new HashMap<>();
        final CSV csv;
        Logger.debug("Loading csv file: " + csvPath);
        try {
            csv = Resources.Instance().load(CSV.class, csvPath);
        } catch (final Resources.ResourceIOException resourceFailure) {
            Logger.warn("Was not able to load csv file: " + resourceFailure);
            return Result.Failure(resourceFailure);
        }

        // parser for propositional formulas
        final NodeReader nodeReader = new NodeReader();
        nodeReader.activateJavaSymbols(); // select the symbols used for parsing conjunction (&&), disjunction (||), ...

        // skip first entry as it is the csv header
        final ListHeadTailView<String[]> rows = new ListHeadTailView<>(csv.rows()).tail();
        for (final String[] row : rows) {
            final CaseSensitivePath pathOfSourceFile = CaseSensitivePath.of(row[0]);
            final Node fileCondition = FixTrueFalse.EliminateTrueAndFalseInplace(nodeReader.stringToNode(row[1]));
            final Node blockCondition = FixTrueFalse.EliminateTrueAndFalseInplace(nodeReader.stringToNode(row[2]));
            // We don't need the actual presenceCondition (lol) as it is a value computed from row[1] and row[2]
            // final Node presenceCondition = nodeReader.stringToNode(row[3]);
            final int startLine = Integer.parseInt(row[4]);
            final int endLine = Integer.parseInt(row[5]);

            /*
            Add the file to our map if not already present and add the
            PreprocessorBlock to it that was described in the parsed row.
             */
            try {
                files.computeIfAbsent(
                        pathOfSourceFile,
                        p -> new SourceCodeFile(fileCondition, p))
                        .addTrace(createAnnotation(blockCondition, startLine, endLine));
            } catch (final Exception e) {
                Logger.warn("Was not able to parse csv file: " + e);
                return Result.Failure(e);
            }
        }

        // sort and return all files as list
        final List<SourceCodeFile> allFiles = new ArrayList<>(files.values());
        allFiles.sort(Comparator.comparing(SourceCodeFile::getFile));
        return Result.Success(new SyntheticArtefactTreeNode<>(allFiles));
    }

    @Override
    public Result<Unit, ? extends Exception> write(final Artefact object, final Path p) {
        /*
        // if (DEBUG) {
            System.out.println(object.prettyPrint());
            object.accept(Debug.createSimpleTreePrinter());
            System.out.println();
        //}//*/

        final ArtefactCSVExporter csvCreator = new ArtefactCSVExporter();
        object.accept(csvCreator);
        final CSV csv = csvCreator.export();

        /*
        // if (DEBUG) {
            System.out.println(csv.toString());
            System.out.println("  ==> " + p);
            System.out.println();
        //}//*/

        return Result.Try(() -> Resources.Instance().write(CSV.class, csv, p));
    }

    protected abstract LineBasedAnnotation createAnnotation(final Node blockCondition, final int startLine, final int endLine);
}
