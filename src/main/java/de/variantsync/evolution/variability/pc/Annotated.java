package de.variantsync.evolution.variability.pc;

import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Annotated {
    private Annotated parent;
    private final List<PreprocessorBlock> blocks = new ArrayList<>();

    public abstract Node getFeatureMapping();
    public abstract Node getPresenceCondition();

    void setParent(Annotated parent) {
        this.parent = parent;
    }
    public Annotated getParent() {
        return parent;
    }

    public void addBlock(PreprocessorBlock b) {
        int left = 0;
        int right = blocks.size();
        int pos = (left + right) / 2;
        while (left < right) {
            final PreprocessorBlock a = blocks.get(pos);

            /*
            #if A
            #endif

            #if B
            #endif
             */
            if (a.getLineTo() < b.getLineFrom()) {
                left = pos + 1;
            }
            /*
            #if B
            #endif

            #if A
            #endif
             */
            else if (b.getLineTo() < a.getLineFrom()) {
                right = pos - 1;
            }
            // there is an overlap
            else {
                final boolean bStartsAfterCurrent = a.getLineFrom() <= b.getLineFrom();
                final boolean bEndsBeforeCurrent = b.getLineTo() <= a.getLineTo();
                /*
                #if A
                  #if B
                  #endif
                #endif
                 */
                if (bStartsAfterCurrent && bEndsBeforeCurrent) {
                    a.addBlock(b);
                    return;
                }
                /*
                #if B
                  #if A
                  #endif
                #endif
                 */
                else if (!bStartsAfterCurrent && !bEndsBeforeCurrent) {
                    // Swap A with B
                    blocks.set(pos, b);
                    b.setParent(this);
                    b.addBlock(a);
                    return;
                }
                /*
                Illegal State: Blocks are overlapping but not nested into each other such as
                /*
                #ifdef A
                  #ifdef B
                #endif // A
                  #endif // B
                or vice versa
                 */
                else {
                    throw new RuntimeException(
                            "Illegal Definition of Preprocessor Block! Given block \""
                                    + b
                                    + "\" overlaps block \""
                                    + a
                                    + "\" but is not contained in it!");
                }
            }

            pos = (left + right) / 2;
        }

        blocks.add(pos, b);
        b.setParent(this);
    }

    protected abstract void prettyPrintHeader(String indent, StringBuilder builder);
    protected abstract void prettyPrintFooter(String indent, StringBuilder builder);

    public String prettyPrint() {
        final StringBuilder builder = new StringBuilder();
        prettyPrint("", builder);
        return builder.toString();
    }

    protected void prettyPrint(String indent, StringBuilder builder) {
        prettyPrintHeader(indent, builder);
        builder.append(System.lineSeparator());
        {
            final String childIndent = indent + "  ";
            for (Annotated child : blocks) {
                child.prettyPrint(childIndent, builder);
            }
        }
        prettyPrintFooter(indent, builder);
        builder.append(System.lineSeparator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Annotated annotated = (Annotated) o;
        return blocks.equals(annotated.blocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, blocks);
    }
}
