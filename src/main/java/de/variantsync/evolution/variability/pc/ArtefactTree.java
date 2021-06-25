package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.fide.FormulaUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import org.prop4j.Node;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents feature traces as a tree structure.
 * That is, nodes in subtrees inherit feature mappings from their ancestors in their presence condition.
 * @param <Child> The type of children that can be added to this tree. Only affects direct children, meaning
 *               that it is not transitive and grandchildren could be of another type.
 */
public class ArtefactTree<Child extends ArtefactTree<?>> implements Artefact {
    private final Node featureMapping;
    private final CaseSensitivePath file;
    private ArtefactTree<?> parent;
    protected final List<Child> subtrees;

    /**
     * Creates a new empty tree (node) with feature mapping True.
     */
    public ArtefactTree() {
        this(FixTrueFalse.True);
    }

    /**
     * Creates a new empty tree (node) with the given feature mapping.
     */
    public ArtefactTree(Node featureMapping) {
        this(featureMapping, null);
    }

    /**
     * Creates a new tree (node) with feature mapping True and the given subtrees.
     */
    public ArtefactTree(List<Child> subtrees) {
        this(FixTrueFalse.True, subtrees, null);
    }

    /**
     * Creates a new tree (node) with feature mapping True and the given subtrees.
     */
    public ArtefactTree(Node featureMapping, CaseSensitivePath file) {
        this(featureMapping, new ArrayList<>(), file);
    }

    /**
     * Creates a new tree (node) with the given feature mapping and subtrees.
     */
    public ArtefactTree(Node featureMapping, List<Child> subtrees, CaseSensitivePath file) {
        this.featureMapping = featureMapping;
        this.subtrees = subtrees;
        this.file = file;
    }

    /**
     * Copy constructor.
     * @param other Object to create a plain copy of (without copying children).
     */
    public ArtefactTree(ArtefactTree<Child> other) {
        this(other.featureMapping, new ArrayList<>(), other.file);
    }

    @Override
    public Node getFeatureMapping() {
        return featureMapping;
    }

    @Override
    public Node getPresenceCondition() {
        final Artefact parent = getParent();
        return parent == null ?
                featureMapping :
                FormulaUtils.AndSimplified(parent.getPresenceCondition(), featureMapping);
    }

    @Override
    public CaseSensitivePath getFile() {
        if (file != null) {
            return file;
        }
        if (parent != null) {
            return parent.getFile();
        }
        return null;
    }

    @Override
    public Result<? extends Artefact, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir) {
        final CaseSensitivePath f = getFile();
        final ArtefactTree<Child> copy = plainCopy();

        if (f != null && !sourceDir.resolve(f).exists()) {
            Logger.error("Skipping file " + f + " as it does not exist!");
        } else {
            for (Child subtree : subtrees) {
                if (variant.isImplementing(subtree.getPresenceCondition())) {
                    final Result<Child, Exception> result = subtree
                            .generateVariant(variant, sourceDir, targetDir)
                            .map(groundTruth -> {
                                final Child carsten = (Child) groundTruth;
                                copy.addTrace(carsten);
                                return carsten;
                            });

                    if (result.isFailure()) {
                        return result;
                    }
                }
            }
        }

        return Result.Success(copy);
    }

    /**
     * Sets the parent of this (sub-)tree.
     * Does not perform relocations and is only used for internal use after other tree operations.
     * @param parent The new parent of this tree.
     */
    void setParent(final ArtefactTree<?> parent) {
        this.parent = parent;
    }

    /**
     * @return The parent of this (sub-)tree.
     */
    public ArtefactTree<?> getParent() {
        return parent;
    }

    public List<Child> getSubtrees() {
        return subtrees;
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
    public String prettyPrint(String indent) {
        final StringBuilder builder = new StringBuilder();
        prettyPrint(indent, builder);
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
    public ArtefactTree<Child> plainCopy() {
        return new ArtefactTree<>(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtefactTree<?> that = (ArtefactTree<?>) o;
        // don't compare parents so we only compare subtrees
        return featureMapping.equals(that.featureMapping) && subtrees.equals(that.subtrees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureMapping, parent, subtrees);
    }
}
