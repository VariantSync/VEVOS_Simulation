package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.nio.file.Path;

/**
 * Represents an artefact that can be annotated with FeatureAnnotations (i.e., line-based feature annotations).
 * In particular, FeatureAnnotations themselves derive Annotated because annotations might be nested:
 *
 * #if A
 *   #if B
 *   ...
 *   #endif
 * #endif
 */
public abstract class Annotated extends ArtefactTree<LineBasedAnnotation> {
    protected Annotated(Node featureMapping) {
        super(featureMapping);
    }
    protected Annotated(Node featureMapping, Path file) {
        super(featureMapping, file);
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
            if (a.getLineTo() < b.getLineFrom()) {
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
                                    + "\" but is not contained in it!");
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
}
