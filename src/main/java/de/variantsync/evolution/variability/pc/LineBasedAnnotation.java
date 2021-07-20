package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.fide.FormulaUtils;
import de.variantsync.evolution.util.functional.Result;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A line-based annotation of source code, such as preprocessor annotations (#ifdef)
 */
public class LineBasedAnnotation extends ArtefactTree<LineBasedAnnotation> {
    private int lineFrom;
    private int lineTo;

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
    public int getLineCount() {
        return lineTo - lineFrom + 1;
    }

    protected void setLineFrom(int lineFrom) {
        this.lineFrom = lineFrom;
    }
    protected void setLineTo(int lineTo) {
        this.lineTo = lineTo;
    }

    @Override
    public void acceptDepthFirst(ArtefactVisitor visitor) {
        visitor.visitLineBasedAnnotation(this);
        for (LineBasedAnnotation child : subtrees) {
            child.acceptDepthFirst(visitor);
        }
    }

    @Override
    public Result<LineBasedAnnotation, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir) {
        throw new UnsupportedOperationException();
    }

    /**
     * Computes all continuous blocks of lines that should be included in the given variant when evaluating the
     * annotations in this artefact.
     * @param variant The variant to get the corresponding lines of code for.
     * @return A set S of intervals where all lines in the given intervals should be included in the given variant.
     *         For any interval [a, b] the lines a, a+1, a+2, ..., b-1, b should be included.
     */
    public List<LineBasedAnnotation> getLinesToGenerateFor(Variant variant, Node partialPC) {
        final List<LineBasedAnnotation> chunksToWrite = new ArrayList<>();
        final int firstCodeLine = lineFrom + 1;
        final int lastCodeLine = lineTo - 1;
        final Node myFeatureMapping = FormulaUtils.AndSimplified(partialPC, getFeatureMapping());

        if (subtrees.size() == 0) {
            // just copy entire file content
            chunksToWrite.add(new LineBasedAnnotation(myFeatureMapping, firstCodeLine, lastCodeLine));
        } else {
            int currentLine = firstCodeLine;
            int currentChildIndex = 0;
            LineBasedAnnotation currentChild;
            while (currentChildIndex < subtrees.size()) {
                currentChild = subtrees.get(currentChildIndex);

                // 1.) Copy all lines of code between currentLine and begin of child.
                int linesToWrite = currentChild.lineFrom - currentLine;
                if (linesToWrite > 0) {
                    chunksToWrite.add(new LineBasedAnnotation(myFeatureMapping, currentLine, currentChild.lineFrom - 1));
                }

                // 2.) handle child
                // TODO: An incremental sat solver would pay off here.
                //       Push and pop the feature mappings.
                if (variant.isImplementing(currentChild.getPresenceCondition())) {
                    chunksToWrite.addAll(currentChild.getLinesToGenerateFor(variant, myFeatureMapping));
                } // else skip child as its lines have to be excluded

                // 3.) go to next child and repeat
                currentLine = currentChild.lineTo + 1;
                ++currentChildIndex;
            }

            if (currentLine <= lastCodeLine) {
                chunksToWrite.add(new LineBasedAnnotation(myFeatureMapping, currentLine, lastCodeLine));
            }
        }

        return chunksToWrite;
    }

    /**
     * Makes all line numbers in the given annotations consecutive.
     * For example, given the following annotations as input
     * [ [3, 4], [8, 13], [20, 21] ]
     * would be reduced to
     * [ [1, 2] [3, 8], [9, 10] ].
     * When the given annotations are complete for a specific variant,
     * this reduction corresponds to turning line numbers in the product line to line numbers in a variant.
     *
     * @param annotations Annotations in an SPL that should be reduced to a variant.
     */
    public static void convertSPLLineNumbersToVariantLineNumbers(List<LineBasedAnnotation> annotations) {
        int currentLine = 1;
        int chunkLength;
        for (LineBasedAnnotation chunk : annotations) {
            chunkLength = chunk.getLineCount();
            chunk.setLineFrom(currentLine);
            currentLine += chunkLength;
            chunk.setLineTo(currentLine - 1);
        }
    }

    public void simplify() {
        LineBasedAnnotationSimplifier.simplify(this);
    }

    /**
     * Merges the given FeatureAnnotation to this artefact in a sorted way.
     * Containment within FeatureAnnotations will be solved recursively, creating a tree structure.
     */
    @Override
    public void addTrace(final LineBasedAnnotation b) {
        int left = 0;
        int right = subtrees.size();
        int pos = (left + right) / 2;
        while (left < right) {
            final LineBasedAnnotation a = subtrees.get(pos);

            /*
            #if A
            #endif

            #if B
            #endif

            ==> Insert b after a.
             */
            if (a.getLineTo() <= b.getLineFrom()) {
                left = pos + 1;
            }
            /*
            #if B
            #endif

            #if A
            #endif

            ==> Insert b before a.
             */
            else if (b.getLineTo() < a.getLineFrom()) {
                right = pos - 1;
            }
            // Otherwise, there is an overlap.
            else {
                final boolean bStartsAfterCurrent = a.getLineFrom() <= b.getLineFrom();
                final boolean bEndsBeforeCurrent = b.getLineTo() <= a.getLineTo();
                /*
                #if A
                  #if B
                  #endif
                #endif

                ==> b is surrounded by (at least) a.
                ==> Insert b to the subtree of a.
                 */
                if (bStartsAfterCurrent && bEndsBeforeCurrent) {
                    a.addTrace(b);
                    return;
                }
                /*
                #if B
                  #if A
                  #endif
                #endif

                ==> b is surrounds at (at least) a.
                ==> Replace the subtree a with b and add a as subtree to b.
                 */
                else if (!bStartsAfterCurrent && !bEndsBeforeCurrent) {
                    // Swap A with B
                    subtrees.set(pos, b);
                    b.setParent(this);
                    b.addTrace(a);
                    return;
                }
                /*
                Illegal State: Blocks are overlapping but not nested into each other such as
                #ifdef A
                  #ifdef B
                #endif // A
                  #endif // B
                or vice versa.
                This is not possible to specify in practice.
                Yet it could happen result from an ill-formed or buggy parsing process that we
                should report by throwing an exception.
                 */
                else {
                    throw new IllegalFeatureTraceSpecification(
                            "Illegal Definition of Preprocessor Block! Given block \""
                                    + b
                                    + "\" overlaps block \""
                                    + a
                                    + "\" in "
                                    + this.getFile()
                                    + " but is not contained in it!");
                }
            }

            pos = (left + right) / 2;
        }

        /*
        We found the location in the list at which to insert b.
         */
        subtrees.add(pos, b);
        b.setParent(this);
    }

    @Override
    public ArtefactTree<LineBasedAnnotation> plainCopy() {
        return new LineBasedAnnotation(this.getFeatureMapping(), this.getLineFrom(), this.getLineTo());
    }

    @Override
    public String toString() {
        return "LineBasedAnnotation{" +
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
