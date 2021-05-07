package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.FormulaUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.list.ListHeadTailView;
import de.variantsync.evolution.variability.pc.PreprocessorBlock;
import de.variantsync.evolution.variability.pc.PresenceConditions;
import de.variantsync.evolution.variability.pc.SourceCodeFile;
import org.prop4j.Node;
import org.prop4j.NodeReader;

import java.nio.file.Path;
import java.util.*;

public class KernelHavenPCLoader implements ResourceLoader<PresenceConditions> {
    @Override
    public boolean canLoad(Path p) {
        return PathUtils.hasExtension(p, ".csv");
    }

    @Override
    public Result<PresenceConditions, Exception> load(Path csvPath) {
        final Map<Path, SourceCodeFile> files = new HashMap<>();
        final CSV csv = Resources.Instance().load(CSV.class, csvPath);
        final NodeReader nodeReader = new NodeReader();
        nodeReader.activateJavaSymbols();

        // skip first entry as it is the csv header
        final ListHeadTailView<String[]> rows = new ListHeadTailView<>(csv.rows()).tail();
        for (final String[] row : rows) {
            final Path pathOfSourceFile = Path.of(row[0]);
            final Node fileCondition = FixTrueFalse.On(nodeReader.stringToNode(row[1]));
            final Node blockCondition = FixTrueFalse.On(nodeReader.stringToNode(row[2]));
            // We don't need this as it is a value computed from row[1] and row[2]
            // final Node presenceCondition = nodeReader.stringToNode(row[3]);
            final int startLine = Integer.parseInt(row[4]);
            final int endLine = Integer.parseInt(row[5]);

            files.computeIfAbsent(
                    pathOfSourceFile,
                    p -> new SourceCodeFile(p, fileCondition))
                    .addBlock(new PreprocessorBlock(blockCondition, startLine, endLine));
        }

        List<SourceCodeFile> allFiles = new ArrayList<>(files.values());
        allFiles.sort(Comparator.comparing(SourceCodeFile::getRelativePath));
        return Result.Success(new PresenceConditions(allFiles));
    }
}
