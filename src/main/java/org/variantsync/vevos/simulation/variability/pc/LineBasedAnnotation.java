package org.variantsync.vevos.simulation.variability.pc;

import org.prop4j.Node;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.AnnotationGroundTruth;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.BlockMatching;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.GroundTruth;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.LineType;
import org.variantsync.vevos.simulation.variability.pc.options.ArtefactFilter;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;
import org.variantsync.vevos.simulation.variability.pc.variantlines.VariantAnnotation;
import org.variantsync.vevos.simulation.variability.pc.variantlines.VariantLine;
import org.variantsync.vevos.simulation.variability.pc.variantlines.VariantLineChunk;
import org.variantsync.vevos.simulation.variability.pc.visitor.LineBasedAnnotationVisitorFocus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A line-based annotation of source code, such as preprocessor annotations (#ifdef)
 */
public class LineBasedAnnotation extends ArtefactTree<LineBasedAnnotation> {
    private final AnnotationStyle style;
    private int lineFrom;
    private int lineTo;
    private LineType lineType;

    /**
     * Creates a new annotation starting at lineFrom and ending at lineTo including both
     * (i.e., [lineFrom, lineTo]).
     * Indexing is 1-based (i.e., the first line in a file is indexed by 1).
     * <p>
     * The style determines whether the annotations is considered to be within the source code (i.e., c macros) or external.
     * Example for a preprocessor block:
     * 3 #if X     <-- lineFrom
     * 4   foo();
     * 5   bar();
     * 6 #endif    <-- lineTo
     * is reflected by LineBasedAnnotation(X, 3, 6, Internal);
     */
    public LineBasedAnnotation(final Node featureMapping, final Node presenceCondition, final LineType lineType, final int lineFrom, final int lineTo, final AnnotationStyle style) {
        super(featureMapping, presenceCondition);
        this.lineType = lineType;
        this.lineFrom = lineFrom;
        this.lineTo = lineTo;
        this.style = style;
    }

    public LineBasedAnnotation(final LineBasedAnnotation other) {
        super(other.getFeatureMapping(), other.getPresenceCondition());
        this.lineType = other.lineType;
        this.lineFrom = other.lineFrom;
        this.lineTo = other.lineTo;
        this.style = other.style;
    }

    private static void addRange(final List<VariantLineChunk> list, final int fromInclusive, final int toInclusive) {
        for (int i = fromInclusive; i <= toInclusive; ++i) {
            list.add(new VariantLine(i));
        }
    }

    public int getLineFrom() {
        return lineFrom;
    }

    protected void setLineFrom(final int lineFrom) {
        this.lineFrom = lineFrom;
    }

    public int getLineTo() {
        return lineTo;
    }

    protected void setLineTo(final int lineTo) {
        this.lineTo = lineTo;
    }

    public int getLineCount() {
        return lineTo - lineFrom + 1;
    }

    public boolean annotates(final int lineNumber) {
        return lineFrom <= lineNumber && lineNumber <= lineTo;
    }

    public boolean isMacro() {
        return style == AnnotationStyle.Internal;
    }

    @Override
    public LineBasedAnnotationVisitorFocus createVisitorFocus() {
        return new LineBasedAnnotationVisitorFocus(this);
    }

    @Override
    public Result<GroundTruth, Exception> generateVariant(
            final Variant variant,
            final CaseSensitivePath sourceDir,
            final CaseSensitivePath targetDir,
            final VariantGenerationOptions strategy) {
        throw new UnsupportedOperationException();
    }

    public Optional<AnnotationGroundTruth> deriveForVariant(final Variant variant, ArtefactFilter<LineBasedAnnotation> annotationFilter) {
        final BlockMatching matching = BlockMatching.MONOID.neutral();
        return deriveForVariant(variant, annotationFilter, 0, matching).map(l -> new AnnotationGroundTruth(this, l, matching));
    }

    private Optional<LineBasedAnnotation> deriveForVariant(final Variant variant, ArtefactFilter<LineBasedAnnotation> annotationFilter, int offset, final BlockMatching matching) {
        if (annotationFilter.shouldKeep(this) && variant.isImplementing(this.getPresenceCondition())) {
            final int firstCodeLine = getLineFrom() + offset;

            /// convert all subtrees to variants
            final List<LineBasedAnnotation> newSubtrees = new ArrayList<>(getNumberOfSubtrees());
            for (final LineBasedAnnotation splAnnotation : subtrees) {
                final Optional<LineBasedAnnotation> mVariantAnnotation = splAnnotation.deriveForVariant(variant, annotationFilter, offset, matching);
                // If the subtree is still present in the variant, it might have shrunk.
                // That can happen when the subtree as nested annotations inside it that code removed.
                if (mVariantAnnotation.isPresent()) {
                    final LineBasedAnnotation variantAnnotation = mVariantAnnotation.get();
                    newSubtrees.add(variantAnnotation);
                    // Check how much the subtree shrunk.
                    offset -= splAnnotation.getLineCount() - variantAnnotation.getLineCount();
                } else {
                    // We removed the subtree entirely so its lines won't take any space in the variant.
                    offset -= splAnnotation.getLineCount();
                }
            }

            final int lastCodeLine = getLineTo() + offset; // ignore #endif
            final LineBasedAnnotation meAsVariant = new LineBasedAnnotation(getFeatureMapping(), getPresenceCondition(), getLineType(), firstCodeLine, lastCodeLine, AnnotationStyle.External);
            meAsVariant.setSubtrees(newSubtrees);
            matching.put(this, meAsVariant);
            return Optional.of(meAsVariant);
        } else {
            return Optional.empty();
        }
    }

    public LineType getLineType() {
        return this.lineType;
    }

    /**
     * Computes all lines that should be included in the given variant when evaluating the annotations in this artefact.
     *
     * @param isIncluded A predicate to select subtrees. A subtree will be considered if isIncluded returns true for it.
     * @return All line numbers that should be copied from the SPL file to the variant file. 1-based.
     */
    public VariantAnnotation getLinesToCopy(final Predicate<LineBasedAnnotation> isIncluded) {
        final List<VariantLineChunk> chunksToWrite = new ArrayList<>();
//        final List<Integer> chunksToWrite = new ArrayList<>();
        final int firstCodeLine = getLineFrom(); // ignore #if
        final int lastCodeLine = getLineTo(); // ignore #endif

        int currentLine = firstCodeLine;
        for (final LineBasedAnnotation subtree : subtrees) {
            if (currentLine < subtree.getLineFrom()) {
                addRange(chunksToWrite, currentLine, subtree.getLineFrom() - 1);
            }

            if (isIncluded.test(subtree)) {
                chunksToWrite.add(subtree.getLinesToCopy(isIncluded));
            }

            currentLine = subtree.getLineTo() + 1;
        }

        if (currentLine <= lastCodeLine) {
            addRange(chunksToWrite, currentLine, lastCodeLine);
        }

        return new VariantAnnotation(
                this.getFeatureMapping(),
                chunksToWrite
        );
    }

    /**
     * This method might no longer work properly with the new GT format and should be used with care.
     */
    @Deprecated
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

    public LineBasedAnnotation plainCopy() {
        return new LineBasedAnnotation(this);
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final LineBasedAnnotation that = (LineBasedAnnotation) o;
        return lineFrom == that.lineFrom && lineTo == that.lineTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lineFrom, lineTo);
    }
}
