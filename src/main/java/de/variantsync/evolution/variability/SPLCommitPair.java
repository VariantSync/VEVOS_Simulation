package de.variantsync.evolution.variability;

public record SPLCommitPair(SPLCommit parent, SPLCommit child) {
    public void clearCaches() {
        parent.clearCaches();
        child.clearCaches();
    }
}
