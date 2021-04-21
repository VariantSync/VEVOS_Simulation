package de.variantsync.repository;

import java.util.List;

public interface IVariabilityRepository extends IRepository {
    List<List<Commit<IVariabilityRepository>>> getCommitSequencesForEvolutionStudy();
}
