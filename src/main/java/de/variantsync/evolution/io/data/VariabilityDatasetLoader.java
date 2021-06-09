package de.variantsync.evolution.io.data;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.CommitIdPair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VariabilityDatasetLoader implements ResourceLoader<VariabilityDataset> {
    private final static String SUCCESS_COMMIT_FILE = "SUCCESS_COMMITS.txt";
    private final static String ERROR_COMMIT_FILE = "ERROR_COMMITS.txt";
    private final static String INCOMPLETE_PC_COMMIT_FILE = "INCOMPLETE_PC_COMMITS.txt";


    /**
     * @param p The path which should be checked.
     * @return true if the path points to a directory that contains at least one of the metadata files, otherwise false.
     */
    @Override
    public boolean canLoad(Path p) {
        try {
            return Files.list(p)
                    .map(Path::toFile)
                    .anyMatch(f -> {
                        String name = f.getName();
                        return name.equals(SUCCESS_COMMIT_FILE) || name.equals(ERROR_COMMIT_FILE) || name.equals(INCOMPLETE_PC_COMMIT_FILE);
                    });
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Result<VariabilityDataset, Exception> load(Path p) {
        List<String> successIds = readLines(p, SUCCESS_COMMIT_FILE);
        List<String> errorIds = readLines(p, ERROR_COMMIT_FILE);
        List<String> incompletePCIds = readLines(p, INCOMPLETE_PC_COMMIT_FILE);

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
    public VariabilityHistory getCommitSequencesForEvolutionStudy() {
        // Retrieve the pairs of usable commits
        Set<CommitIdPair> usableCommitIdPairs = this.getCommitPairsForEvolutionStudy();
        // Create lists for the commits in the pairs and merge lists according to parent-child relationships
        Map<VariabilityCommit, LinkedList<VariabilityCommit>> commitToCommitSequenceMap = new HashMap<>();
        for (CommitIdPair pair : usableCommitIdPairs) {
            final boolean parentHasSequence = commitToCommitSequenceMap.containsKey(pair.parent());
            final boolean childHasSequence = commitToCommitSequenceMap.containsKey(pair.child());
            if (parentHasSequence && childHasSequence) {
                // Parent and child already belong to a list
                // Merge the two lists, if they are not the same list
                final var parentList = commitToCommitSequenceMap.get(pair.parent());
                final var childList = commitToCommitSequenceMap.get(pair.child());
                if (parentList == childList) {
                    throw new IllegalStateException("The same parent-child pair was considered twice.");
                }
                // Add all commits from the child list to the parent list, and replace the list in the map
                parentList.addAll(childList);
                // Now, update the associated list for each added commit
                childList.forEach(c -> commitToCommitSequenceMap.put(c, parentList));
            } else if (parentHasSequence) {
                // Only the parent belongs to a list
                // Append the child to the list
                final var commitList = commitToCommitSequenceMap.get(pair.parent());
                commitList.addLast(pair.child());
                commitToCommitSequenceMap.put(pair.child(), commitList);
            } else if (childHasSequence) {
                // Only the child belongs to a list
                // Prepend the parent to the list
                final var commitList = commitToCommitSequenceMap.get(pair.child());
                commitList.addFirst(pair.parent());
                commitToCommitSequenceMap.put(pair.parent(), commitList);
            } else {
                // Neither parent nor child were added to a list
                // Create a new list that contains parent and child
                final LinkedList<VariabilityCommit> commitList = new LinkedList<>();
                commitList.addLast(pair.parent());
                commitList.addLast(pair.child());
                commitToCommitSequenceMap.put(pair.parent(), commitList);
                commitToCommitSequenceMap.put(pair.child(), commitList);
            }
        }

        // Lastly, build a VariabilityHistory instance from the collected lists
        NonEmptyList<NonEmptyList<VariabilityCommit>> history = null;
        for (LinkedList<VariabilityCommit> commitList : new HashSet<>(commitToCommitSequenceMap.values())) {
            NonEmptyList<VariabilityCommit> commitSequence = new NonEmptyList<>(commitList);
            if (history == null) {
                LinkedList<NonEmptyList<VariabilityCommit>> tempList = new LinkedList<>();
                tempList.add(commitSequence);
                history = new NonEmptyList<>(tempList);
            } else {
                history.add(commitSequence);
            }
        }
        return new VariabilityHistory(history);
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
    public Set<CommitIdPair> getCommitPairsForEvolutionStudy() {
        return successCommits.stream()
                // We only consider commits that did not process a merge
                .filter(nonMergeCommits::contains)
                // We only consider commits that processed an SPL commit whose parent was also processed
                .filter(c -> {
                    VariabilityCommit[] parents = c.getEvolutionParents();
                    return parents.length == 1 && successCommits.contains(parents[0]);
                })
                .map(c -> new CommitIdPair(c.getEvolutionParents()[0], c))
                .collect(Collectors.toSet());
    }

    private List<String> readLines(Path p, String fileName) {
        try {
            return Files.readAllLines(p.resolve(fileName));
        } catch (IOException e) {
            Logger.debug("Was not able to read file " + p.resolve(fileName));
            return Collections.emptyList();
        }
    }
}
