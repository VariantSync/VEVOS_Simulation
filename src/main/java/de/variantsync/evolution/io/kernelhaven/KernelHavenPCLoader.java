package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.io.ResourceWriter;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.util.list.ListHeadTailView;
import de.variantsync.evolution.variability.pc.*;
import de.variantsync.evolution.variability.pc.visitor.common.Debug;
import org.prop4j.Node;
import org.prop4j.NodeReader;

import java.nio.file.Path;
import java.util.*;

/**
 * Class to load presence conditions from the csv file generated by KernelHaven.
 */
public class KernelHavenPCLoader implements ResourceLoader<Artefact>, ResourceWriter<Artefact> {
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
            int startLine = Integer.parseInt(row[4]);
            final int endLine = Integer.parseInt(row[5]) + 1 /* to include #endif */;

            /// If a block starts at 1 in KernelHaven files, it does not denote an #if but the entire file.
            /// Thus, there is no #if at line 1 but LineBasedAnnotation expects a macro at startLine.
            /// Thus, imagine a macro at line 0, that does not exist.
            if (startLine == 1) {
                startLine = 0;
            }

            /*
            Add the file to our map if not already present and add the
            PreprocessorBlock to it that was described in the parsed row.
             */
            try {
                files.computeIfAbsent(
                        pathOfSourceFile,
                        p -> new SourceCodeFile(fileCondition, p))
                        .addTrace(new LineBasedAnnotation(blockCondition, startLine, endLine));
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
        System.out.println(object.prettyPrint());
        object.accept(Debug.createSimpleTreePrinter());
        System.out.println();

        final ArtefactToCSVVisitor csvCreator = new ArtefactToCSVVisitor();
        object.accept(csvCreator);
        final CSV csv = csvCreator.export();

        System.out.println(csv.toString());
        System.out.println("  ==> " + p);
        System.out.println();

        return Result.Try(() -> Resources.Instance().write(CSV.class, csv, p));
    }
}
