package de.variantsync.evolution.repository;

import de.variantsync.evolution.variants.VariantCommit;
import de.variantsync.evolution.variants.VariantsRevision;

import java.util.Optional;

/**
 * Repository that mimics the evolution of an ISPLRepository in terms of variants instead of an integrated code base.
 */
public interface IVariantsRepository extends IRepository<VariantCommit> {
    /**
     * Commits the current state of the repository with the given message.
     * @param message Message for the commit to make.
     * @return A handle for the commit that was just created. Returns empty if there were no changes to commit.
     */
    Optional<VariantCommit> commit(String message);

    /**
     * Returns a branch handle form a branches name.
     * @param name Name of the branch to obtained metadata of.
     * @return Branch object representing the branch with the given name.
     */
    Branch getBranchByName(String name);

    /**
     * @return The first revision of variants of this repository. Empty, iff there is nothing to generate.
     */
    Optional<VariantsRevision> getStartRevision();
}
