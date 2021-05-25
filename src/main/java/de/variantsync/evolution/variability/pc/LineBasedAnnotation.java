package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import org.prop4j.Node;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A line-based annotation of source code, such as preprocessor annotations (#ifdef)
 */
public class LineBasedAnnotation extends Annotated {
    private final int lineFrom;
    private final int lineTo;

    /**
     * Creates a new annotations starting at lineFrom (inclusive @Alex?) and ending at lineTo (inclusive@Alex?).
     * TODO: @Alex: Indexing is zero based?
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
    public Result<Unit, Exception> project(Variant variant, Path sourceDir, Path targetDir) {
        final Path sourceFile = sourceDir.resolve(getFile());
        final Path targetFile = targetDir.resolve(getFile());

        // TODO: Cache all lines to write and then write them in one go.
        if (subtrees.size() == 0) {
            // just copy entire file content
            return Result.Try(() -> TextIO.CopyTextLines(sourceFile, targetFile, lineFrom, lineTo));
        } else {
            int currentLine = lineFrom;
            int currentChildIndex = 0;
            LineBasedAnnotation currentChild;
            while (currentChildIndex < subtrees.size()) {
                currentChild = subtrees.get(currentChildIndex);

                // 1.) Copy all LOC between currentLine and begin of child
                int currentChunkEnd = currentChild.lineFrom;
                int linesToWrite = currentChunkEnd - currentLine;
                if (linesToWrite > 0) {
                    final int currentLineForUseInLambda = currentLine;
                    final Result<Unit, Exception> res = Result.Try(() ->
                        TextIO.CopyTextLines(sourceFile, targetFile, currentLineForUseInLambda, currentChunkEnd - 1));
                    if (res.isFailure()) { return res; }
                }
                // handle child
                if (variant.isImplementing(currentChild.getPresenceCondition())) {
                    final var res = currentChild.project(variant, sourceDir, targetDir);
                    if (res.isFailure()) { return res; }
                } // else skip child as its lines have to be exluded

                // go to next child and repeat
                currentLine = currentChild.lineTo + 1;
                ++currentChildIndex;
            }

            if (currentLine <= lineTo) {
                final int currentLineForUseInLambda = currentLine;
                final Result<Unit, Exception> res= Result.Try(() ->
                    TextIO.CopyTextLines(sourceFile, targetFile, currentLineForUseInLambda, lineTo));
                if (res.isFailure()) { return res; }
            }
        }

        return Result.Success(Unit.Instance());
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
