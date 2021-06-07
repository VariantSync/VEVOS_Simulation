package de.variantsync.evolution.io.data;

import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.variability.SPLCommit;

import java.util.*;

public class VariabilityMetadata {
    private final List<SPLCommit> successCommits;
    private final List<SPLCommit> errorCommits;
    private final List<SPLCommit> incompletePCCommits;

    // Package-private constructor that should only be called by a corresponding resource loader
    // TODO: Reconsider metadata structure w.r.t. new extraction output comprising the parent ids
    VariabilityMetadata(SuccessCommits successCommits, ErrorCommits errorCommits, IncompletePCCommits incompletePCCommits) {
        this.successCommits = successCommits.commits;
        this.errorCommits = errorCommits.commits;
        this.incompletePCCommits = incompletePCCommits.commits;
    }

    /**
     *
     * @return List of SPL-commits for which the variability extraction was successful.
     */
    public List<SPLCommit> getSuccessCommits() {
        return new ArrayList<>(successCommits);
    }

    /**
     *
     * @return List of SPL-commits for which the variability extraction was unsuccessful.
     */
    public List<SPLCommit> getErrorCommits() {
        return new ArrayList<>(errorCommits);
    }

    /**
     *
     * @return List of SPL-commits for which the variability extraction was successful, but extracted PCs are incomplete.
     */
    public List<SPLCommit> getIncompletePCCommits() {
        // TODO: Write tests
        return new ArrayList<>(incompletePCCommits);
    }

    public SPLCommit[] getEvolutionParents(String s) {
        // TODO Implement
        throw new NotImplementedException();
    }

    public VariabilityHistory getCommitSequencesForEvolutionStudy() {
        // TODO Implement
        throw new NotImplementedException();
    }

    // Package-private records to ensure correct initialization by a ResourceLoader
    record SuccessCommits(List<SPLCommit> commits) {}
    record ErrorCommits(List<SPLCommit> commits) {}
    record IncompletePCCommits(List<SPLCommit> commits) {}

}
