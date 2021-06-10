package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
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
     * Creates a new annotations starting at lineFrom and ending at lineTo.
     * [lineFrom, lineTo]
     * Indexing is 0-based.
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
