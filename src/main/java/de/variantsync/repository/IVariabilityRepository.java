package de.variantsync.repository;

import de.variantsync.subjects.VariabilityCommit;

import java.nio.file.Path;

public interface IVariabilityRepository extends IRepository<VariabilityCommit> {
    VariabilityHistory getCommitSequencesForEvolutionStudy();

    Path getFeatureModelFile();
    Path getVariabilityFile();
}
