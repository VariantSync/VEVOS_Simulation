package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Functional;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.visitor.ArtefactVisitorFocus;
import de.variantsync.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;

import java.util.ArrayList;
import java.util.List;

/**
 * A synthetic node in the artefact tree that does not need to have a physical counterpart (e.g., file or annotation).
 * @param <Child> The type of children this node can have.
 */
public class SyntheticArtefactTreeNode<Child extends ArtefactTree<?>> extends ArtefactTree<Child> {
    /**
     * Creates a new empty tree (node) with feature mapping True.
     */
    public SyntheticArtefactTreeNode() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new tree (node) with feature mapping True and the given subtrees.
     */
    public SyntheticArtefactTreeNode(List<Child> subtrees) {
        super(FixTrueFalse.True, subtrees, null);
    }

    /**
     * Plain copy constructor.
     * @param other Object to create a plain copy of (without copying children).
     */
    public SyntheticArtefactTreeNode(ArtefactTree<Child> other) {
        super(other.getFeatureMapping(), new ArrayList<>(), other.getFile());
    }

    @Override
    public ArtefactVisitorFocus<? extends Artefact> createVisitorFocus() {
        return new SyntheticArtefactTreeNodeVisitorFocus<>(this);
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
                            .map(Functional::uncheckedCast);
                    result.ifSuccess(copy::addTrace);
                    if (result.isFailure()) {
                        return result;
                    }
                }
            }
        }

        return Result.Success(copy);
    }

    @Override
    public String toString() {
        return "SyntheticArtefactTreeNode{" +
                "featureMapping=" + getFeatureMapping() +
                '}';
    }

    public SyntheticArtefactTreeNode<Child> plainCopy() {
        return new SyntheticArtefactTreeNode<>(this);
    }
}
