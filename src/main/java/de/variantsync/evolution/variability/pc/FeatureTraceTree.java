package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.util.fide.FormulaUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents feature traces as a tree structure.
 * That is, nodes in subtrees inherit feature mappings from their ancestors in their presence condition.
 * @param <Child> The type of children that can be added to this tree. Only affects direct children, meaning
 *               that it is not transitive and grandchildren could be of another type.
 */
public class FeatureTraceTree<Child extends FeatureTraceTree<?>> implements FeatureTrace {
    private final Node featureMapping;

    private FeatureTraceTree<?> parent;
    protected final List<Child> subtrees;

    /**
     * Creates a new empty tree (node) with feature mapping True.
     */
    public FeatureTraceTree() {
        this(FixTrueFalse.True);
    }

    /**
     * Creates a new empty tree (node) with the given feature mapping.
     */
    public FeatureTraceTree(Node featureMapping) {
        this(featureMapping, new ArrayList<>());
    }

    /**
     * Creates a new tree (node) with feature mapping True and the given subtrees.
     */
    public FeatureTraceTree(List<Child> subtrees) {
        this(FixTrueFalse.True, subtrees);
    }

    /**
     * Creates a new tree (node) with the given feature mapping and subtrees.
     */
    public FeatureTraceTree(Node featureMapping, List<Child> subtrees) {
        this.featureMapping = featureMapping;
        this.subtrees = subtrees;
    }

    @Override
    public Node getFeatureMapping() {
        return featureMapping;
    }

    @Override
    public Node getPresenceCondition() {
        final FeatureTrace parent = getParent();
        return parent == null ?
                featureMapping :
                FormulaUtils.AndSimplified(parent.getPresenceCondition(), featureMapping);
    }

    @Override
    public FeatureTrace project(Variant variant) {
        // TODO: Implement Issue #2 here.
        throw new NotImplementedException();
    }

    /**
     * Sets the parent of this (sub-)tree.
     * Does not perform relocations and is only used for internal use after other tree operations.
     * @param parent The new parent of this tree.
     */
    void setParent(final FeatureTraceTree<?>  parent) {
        this.parent = parent;
    }

    /**
     * @return The parent of this (sub-)tree.
     */
    public FeatureTraceTree<?> getParent() {
        return parent;
    }

    /**
     * Adds the given subtree to this tree.
     * Behaviour might change based on subclasses (e.g., for Annotated).
     * @param child The subtree to add.
     */
    public void addTrace(final Child child) {
        subtrees.add(child);
        child.setParent(this);
    }

    /**
     * Prints meta information to the given builder that should be displayed before printing all children.
     * @param indent The current indent of the output string to show the tree in a convenient way.
     */
    protected void prettyPrintHeader(String indent, StringBuilder builder) {
        builder.append(indent).append("[");
    }

    /**
     * Prints meta information to the given builder that should be displayed after printing all children.
     * @param indent The current indent of the output string to show the tree in a convenient way.
     */
    protected void prettyPrintFooter(String indent, StringBuilder builder) {
        builder.append(indent).append("]");
    }

    @Override
    public String prettyPrint() {
        final StringBuilder builder = new StringBuilder();
        prettyPrint("", builder);
        return builder.toString();
    }

    protected void prettyPrint(String indent, StringBuilder builder) {
        // print node info (e.g., "[")
        prettyPrintHeader(indent, builder);
        builder.append(System.lineSeparator());
        // print all subtrees
        {
            final String childIndent = indent + "  ";
            for (Child child : subtrees) {
                child.prettyPrint(childIndent, builder);
            }
        }
        // print end (e.g., "]")
        prettyPrintFooter(indent, builder);
        builder.append(System.lineSeparator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureTraceTree<?> that = (FeatureTraceTree<?>) o;
        // don't compare parents so we only compare subtrees
        return featureMapping.equals(that.featureMapping) && subtrees.equals(that.subtrees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureMapping, parent, subtrees);
    }
}
