package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.io.ResourceWriter;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.util.list.ListHeadTailView;
import de.variantsync.evolution.variability.pc.*;
import org.prop4j.Node;
import org.prop4j.NodeReader;

import java.nio.file.Path;
import java.util.*;

/**
 * Class to load presence conditions from the csv file generated by KernelHaven.
 */
public class KernelHavenPCIO implements ResourceLoader<Artefact>, ResourceWriter<Artefact> {
    @Override
    public boolean canLoad(Path p) {
        return PathUtils.hasExtension(p, ".csv");
    }

    @Override
    public boolean canWrite(Path p) {
        return canLoad(p);
    }

    @Override
    public Result<Artefact, Exception> load(Path csvPath) {
        final Map<CaseSensitivePath, SourceCodeFile> files = new HashMap<>();
        final CSV csv;
        try {
            csv = Resources.Instance().load(CSV.class, csvPath);
        } catch (Resources.ResourceIOException resourceFailure) {
            return Result.Failure(resourceFailure);
        }

        // parser for propositional formulas
        final NodeReader nodeReader = new NodeReader();
        nodeReader.activateJavaSymbols(); // select the symbols used for parsing conjunction (&&), disjunction (||), ...

        // skip first entry as it is the csv header
        final ListHeadTailView<String[]> rows = new ListHeadTailView<>(csv.rows()).tail();
        for (final String[] row : rows) {
            final CaseSensitivePath pathOfSourceFile = CaseSensitivePath.of(row[0]);
            final Node fileCondition = FixTrueFalse.On(nodeReader.stringToNode(row[1]));
            final Node blockCondition = FixTrueFalse.On(nodeReader.stringToNode(row[2]));
            // We don't need the actual presenceCondition (lol) as it is a value computed from row[1] and row[2]
            // final Node presenceCondition = nodeReader.stringToNode(row[3]);
            final int startLine = Integer.parseInt(row[4]);
            int endLine = Integer.parseInt(row[5]);

            /// If a block starts at 1 in KernelHaven files, it does not denote an #if but the entire file.
            /// Thus, there is no #if at line 1 but LineBasedAnnotation expects a macro at startLine.
            final boolean isVirtualSurroundingTrue = startLine == 1;

            /*
            Line numbers for the all surrounding true are given in [1, last line of file + 1] format.
            Line numbers for macros are given in [#if, #endif) format.
             */
            if (isVirtualSurroundingTrue) {
                // If we find the all-surrounding "true" macro:
                // We know that it always points behind one line behind the last line.
                endLine -= 1;
            } else {
                endLine += 1 /* to include #endif */;
            }

            /*
            Add the file to our map if not already present and add the
            PreprocessorBlock to it that was described in the parsed row.
             */
            try {
                files.computeIfAbsent(
                        pathOfSourceFile,
                        p -> new SourceCodeFile(fileCondition, p))
                        .addTrace(new LineBasedAnnotation(blockCondition, startLine, endLine, !isVirtualSurroundingTrue));
            } catch (Exception e) {
                return Result.Failure(e);
            }
        }

        // sort and return all files as list
        List<SourceCodeFile> allFiles = new ArrayList<>(files.values());
        allFiles.sort(Comparator.comparing(SourceCodeFile::getFile));
        return Result.Success(new SyntheticArtefactTreeNode<>(allFiles));
    }

    @Override
    public Result<Unit, ? extends Exception> write(Artefact object, Path p) {
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
}
