package de.variantsync.repository;

import de.variantsync.evolution.VariantsCommit;

public interface IVariantsRepository extends IRepository<VariantsCommit> {
    VariantsCommit commit(String message);
    Branch getBranchByName(String name);
}
