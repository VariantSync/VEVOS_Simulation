package de.variantsync.repository;

import de.variantsync.evolution.VariantsCommit;
import de.variantsync.evolution.VariantsRevision;

import java.util.Optional;

public interface IVariantsRepository extends IRepository<VariantsCommit> {
    VariantsCommit commit(String message);
    Branch getBranchByName(String name);

    Optional<VariantsRevision> getStartRevision();
}
