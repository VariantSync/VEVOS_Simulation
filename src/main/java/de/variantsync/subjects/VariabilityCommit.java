package de.variantsync.subjects;

import de.variantsync.repository.Commit;
import de.variantsync.repository.IVariabilityRepository;

public class VariabilityCommit extends Commit<IVariabilityRepository> {
    private final SPLCommit origin;
    private VariabilityCommit[] evolutionParents;

    public VariabilityCommit(String commitId, SPLCommit splCommit) {
        super(commitId);
        origin = splCommit;
    }

    void setEvolutionParents(VariabilityCommit[] evolutionParents) {
        this.evolutionParents = evolutionParents;
    }

    /**
     * Each commit in the variability repo was responsible for processing one commit from the SPL repo. This method
     * returns the commits from the variability repo that processed the parent commits in the SPL repo of the SPL commit
     * that was processed by this commit.
     * <p>
     * Note that these are NOT the parents of the commit in the variability repository.
     *
     * @return Commits that processed the parent commits in the SPL history
     */
    public VariabilityCommit[] getEvolutionParents() {
        return evolutionParents;
    }

    /**
     * Get the SPL commit that was processed by this commit from the variability repo
     */
    public SPLCommit splCommit() {
        return origin;
    }
}

