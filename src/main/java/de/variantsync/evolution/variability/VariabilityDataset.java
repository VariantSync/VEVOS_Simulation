package de.variantsync.evolution.variability;

import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.list.NonEmptyList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VariabilityDataset {
    private final Set<SPLCommit> allCommits;
    private final List<SPLCommit> successCommits;
    private final List<SPLCommit> errorCommits;
    private final List<SPLCommit> partialSuccessCommits;

    public VariabilityDataset(@NotNull List<SPLCommit> successCommits, @NotNull List<SPLCommit> errorCommits, @NotNull List<SPLCommit> partialSuccessCommits) {
        this.successCommits = successCommits;
        this.errorCommits = errorCommits;
        this.partialSuccessCommits = partialSuccessCommits;
        this.allCommits = new HashSet<>();
        this.allCommits.addAll(successCommits);
        this.allCommits.addAll(errorCommits);
        this.allCommits.addAll(partialSuccessCommits);
        if (allCommits.size() != successCommits.size() + errorCommits.size() + partialSuccessCommits.size()) {
            String errorMessage = "Some of the dataset's commits belong to more than one category (SUCCESS | ERROR | INCOMPLETE_PC)";
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * @return A Set of all commits contained in this dataset.
     */
    public Set<SPLCommit> getAllCommits() {
        return allCommits;
    }

    /**
     * @return A List of all commits for which variability data was extracted successfully.
     */
    public List<SPLCommit> getSuccessCommits() {
        return successCommits;
    }

    /**
     * @return A List of all commits for which variability data could not be extracted.
     */
    public List<SPLCommit> getErrorCommits() {
        return errorCommits;
    }

    /**
     * @return A List of all commits for which variability data was extracted partially.
     */
    public List<SPLCommit> getPartialSuccessCommits() {
        return partialSuccessCommits;
    }

    /**
     * Retrieve all sequences of usable commits in form of a VariabilityHistory instance.
     *
     * <p>
     * The retrieved VariabilityHistory is a list that contains commit sequences that can be used for our evolution study. Each
     * sequence consists of at least two commits. A sequence is a list of commits, where a commit at index
     * i is the logical parent of the commit at index i+1.
     * </p>
     *
     * @return All sequences of commits that are usable in our evolution study.
     */
    public VariabilityHistory getVariabilityHistory(Function<Collection<SPLCommit>,  List<NonEmptyList<SPLCommit>>> sequenceExtractor) {
        // Build a VariabilityHistory instance by applying the provided function to the set of success commits
        List<NonEmptyList<SPLCommit>> history = sequenceExtractor.apply(this.successCommits);

        if (history.isEmpty()) {
            Logger.error("There is no valid sequence of commits from which a VariabilityHistory can be built!");
            throw new IllegalStateException();
        } else {
            return new VariabilityHistory(new NonEmptyList<>(history));
        }
    }

    /**
     * Return the set of commit pairs (childCommit, parentCommit) for which the following holds:
     * <ul>
     *     <li>
     *         childCommit processed its corresponding SPL commit successfully, i.e., feature model and code variability was extracted
     *     </li>
     *     <li>
     *         The SPL commit, which was processed by childCommit, has exactly one parent commit in the SPL history (it is no merge commit)
     *     </li>
     *     <li>
     *         The parent of the SPL commit was also processed successfully and is represented by parentCommit
     *     </li>
     * </ul>
     *
     * @return Set of commit pairs that can be used in a variability evolution study
     */
    public Set<CommitPair<SPLCommit>> getCommitPairsForEvolutionStudy() {
        return successCommits.stream()
                .map(c -> {
                    if (c.parents().isPresent()) {
                        SPLCommit[] parents = c.parents().get();
                        // We only consider commits that did not process a merge
                        boolean notAMerge = parents.length == 1;
                        SPLCommit p = parents[0];
                        // We only consider commits that processed an SPL commit whose parent was also processed
                        boolean parentSuccess = successCommits.contains(p);
                        if (notAMerge && parentSuccess) {
                            return new CommitPair<>(p, c);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}