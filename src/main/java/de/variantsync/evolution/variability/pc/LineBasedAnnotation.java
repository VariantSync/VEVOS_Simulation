package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.util.math.IntervalSet;
import org.prop4j.Node;

import java.util.Objects;

/**
 * A line-based annotation of source code, such as preprocessor annotations (#ifdef)
 */
public class LineBasedAnnotation extends Annotated {
    private final int lineFrom;
    private final int lineTo;

    /**
     * Creates a new annotation starting at lineFrom and ending at lineTo including both
     * (i.e., [lineFrom, lineTo]).
     * Indexing is 1-based (i.e., the first line in a file is indexed by 1).
     * Example for a preprocessor block
     * 3 #if X     <-- lineFrom
     * 4   foo();
     * 5   bar();
     * 6 #endif    <-- lineTo
     * is reflected by LineBasedAnnotation(X, 3, 6);
     */
    public LineBasedAnnotation(Node featureMapping, int lineFrom, int lineTo) {
        super(featureMapping);
        this.lineFrom = lineFrom;
        this.lineTo = lineTo;
    }

    public int getLineFrom() {
        return lineFrom;
    }
    public int getLineTo() {
        return lineTo;
    }

    @Override
    public Result<Unit, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir) {
        final CaseSensitivePath sourceFile = sourceDir.resolve(getFile());
        final CaseSensitivePath targetFile = targetDir.resolve(getFile());
        final IntervalSet chunksToWrite    = getLinesToGenerateFor(variant);
        return Result.Try(() -> TextIO.CopyTextLines(sourceFile.path(), targetFile.path(), chunksToWrite));
    }

    /**
     * Computes all continuous blocks of lines that should be included in the given variant when evaluating the
     * annotations in this artefact.
     * @param variant The variant to get the corresponding lines of code for.
     * @return A set S of intervals where all lines in the given intervals should be included in the given variant.
     *         For any interval [a, b] the lines a, a+1, a+2, ..., b-1, b should be included.
     */
    public IntervalSet getLinesToGenerateFor(Variant variant) {
        final IntervalSet chunksToWrite = new IntervalSet();
        final int firstCodeLine = lineFrom + 1;
        final int lastCodeLine = lineTo - 1;

        if (subtrees.size() == 0) {
            // just copy entire file content
            chunksToWrite.add(firstCodeLine, lastCodeLine);
        } else {
            int currentLine = firstCodeLine;
            int currentChildIndex = 0;
            LineBasedAnnotation currentChild;
            while (currentChildIndex < subtrees.size()) {
                currentChild = subtrees.get(currentChildIndex);

                // 1.) Copy all LOC between currentLine and begin of child
                int linesToWrite = currentChild.lineFrom - currentLine;
                if (linesToWrite > 0) {
                    chunksToWrite.add(currentLine, currentChild.lineFrom - 1);
                }

                // handle child
                if (variant.isImplementing(currentChild.getPresenceCondition())) {
                    chunksToWrite.mappendInline(currentChild.getLinesToGenerateFor(variant));
                } // else skip child as its lines have to be exluded

                // go to next child and repeat
                currentLine = currentChild.lineTo + 1;
                ++currentChildIndex;
            }

            if (currentLine <= lastCodeLine) {
                chunksToWrite.add(currentLine, lastCodeLine);
            }
        }

        return chunksToWrite;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "featureMapping=" + getFeatureMapping() +
                ", from " + lineFrom +
                " to " + lineTo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LineBasedAnnotation that = (LineBasedAnnotation) o;
        return lineFrom == that.lineFrom && lineTo == that.lineTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lineFrom, lineTo);
    }

    @Override
    protected void prettyPrintHeader(String indent, StringBuilder builder) {
        builder.append(indent).append("#if ").append(getFeatureMapping()).append(" @").append(getLineFrom());
    }

    @Override
    protected void prettyPrintFooter(String indent, StringBuilder builder) {
        builder.append(indent).append("#endif @").append(getLineTo());
    }
}
