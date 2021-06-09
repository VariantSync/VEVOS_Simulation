package de.variantsync.evolution.io.data;

import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.variability.SPLCommit;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariabilityDataset {
    private final Set<SPLCommit> allCommits;
    private final List<SPLCommit> successCommits;
    private final List<SPLCommit> errorCommits;
    private final List<SPLCommit> incompletePCCommits;

    public VariabilityDataset(@NotNull List<SPLCommit> successCommits, @NotNull List<SPLCommit> errorCommits, @NotNull List<SPLCommit> incompletePCCommits) {
        this.successCommits = successCommits;
        this.errorCommits = errorCommits;
        this.incompletePCCommits = incompletePCCommits;
        this.allCommits = new HashSet<>();
        this.allCommits.addAll(successCommits);
        this.allCommits.addAll(errorCommits);
        this.allCommits.addAll(incompletePCCommits);
        if (allCommits.size() != successCommits.size() + errorCommits.size() + incompletePCCommits.size()) {
            Logger.error("Some of the dataset's commits belong to more than one category (SUCCESS | ERROR | INCOMPLETE_PC)");
            throw new IllegalArgumentException("Some of the dataset's commits belong to more than one category (SUCCESS | ERROR | INCOMPLETE_PC)");
        }
    }

    public Set<SPLCommit> getAllCommits() {
        return allCommits;
    }

    public List<SPLCommit> getSuccessCommits() {
        return successCommits;
    }

    public List<SPLCommit> getErrorCommits() {
        return errorCommits;
    }

    public List<SPLCommit> getIncompletePCCommits() {
        return incompletePCCommits;
    }
}
