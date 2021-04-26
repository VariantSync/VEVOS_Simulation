package de.variantsync.repository;

import de.variantsync.evolution.VariantCommit;
import de.variantsync.evolution.VariantsRevision;

import java.util.Optional;

public interface IVariantsRepository extends IRepository<VariantCommit> {
    Optional<VariantCommit> commit(String message);
    Branch getBranchByName(String name);

    Optional<VariantsRevision> getStartRevision();
}
