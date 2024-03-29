package org.variantsync.vevos.simulation.variability.pc;

import org.variantsync.functjonal.Cast;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.feature.Variant;
import org.tinylog.Logger;
import org.variantsync.vevos.simulation.util.fide.bugfix.FixTrueFalse;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.GroundTruth;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;
import org.variantsync.vevos.simulation.variability.pc.visitor.ArtefactVisitorFocus;
import org.variantsync.vevos.simulation.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;

import java.io.FileNotFoundException;
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
    public SyntheticArtefactTreeNode(final List<Child> subtrees) {
        super(FixTrueFalse.True, FixTrueFalse.True, subtrees, null);
    }

    /**
     * Plain copy constructor.
     * @param other Object to create a plain copy of (without copying children).
     */
    public SyntheticArtefactTreeNode(final ArtefactTree<Child> other) {
        super(other.getFeatureMapping(), other.getPresenceCondition(), new ArrayList<>(), other.getFile());
    }

    @Override
    public ArtefactVisitorFocus<? extends Artefact> createVisitorFocus() {
        return new SyntheticArtefactTreeNodeVisitorFocus<>(this);
    }

    @Override
    public Result<GroundTruth, Exception> generateVariant(final Variant variant, final CaseSensitivePath sourceDir, final CaseSensitivePath targetDir, final VariantGenerationOptions strategy) {
        final CaseSensitivePath f = getFile();
        final SyntheticArtefactTreeNode<Child> copy = plainCopy();
        final GroundTruth groundTruth = GroundTruth.withoutAnnotations(copy);

        if (f != null && !sourceDir.resolve(f).exists()) {
            Logger.error("Skipping file " + f + " as it does not exist!");
            if (strategy.exitOnError() && !strategy.ignoreNonExistentSPLFiles()) {
                return Result.Failure(new FileNotFoundException(f + " does not exist!"));
            }
        } else {
            for (final Child subtree : subtrees) {
                if (subtree instanceof SourceCodeFile sourceCodeFile) {
                    if (!strategy.filter().shouldKeep(sourceCodeFile)) {
                        continue;
                    }
                }

                if (variant.isImplementing(subtree.getPresenceCondition())) {
                    final Result<GroundTruth, Exception> result = subtree
                            .generateVariant(variant, sourceDir, targetDir, strategy);

                    result.ifSuccess(childGroundTruth -> {
                        copy.addTrace(Cast.unchecked(childGroundTruth.variant()));
                        groundTruth.add(childGroundTruth);
                    });

                    if (result.isFailure()) {
                        if (
                                strategy.exitOnError()
                                && !(strategy.ignoreNonExistentSPLFiles() && result.getFailure() instanceof FileNotFoundException)
                        ) {
                            return result;
                        } else {
                            Logger.error(result.getFailure().getMessage());
                        }
                    }
                }
            }
        }

        return Result.Success(groundTruth);
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
