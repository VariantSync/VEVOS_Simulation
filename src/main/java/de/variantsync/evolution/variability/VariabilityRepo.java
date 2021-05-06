package de.variantsync.evolution.variability;

import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.repository.Branch;
import de.variantsync.evolution.repository.IVariabilityRepository;
import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.util.list.NonEmptyList;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.lang.String;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static de.variantsync.evolution.variability.Constants.CURRENT_COMMIT_FILE;
import static de.variantsync.evolution.variability.Constants.ERROR_FILE;

/**
 * Record for storing meta data about the SPL commits that were processed by a variability extraction.
 * <p>
 * More specifically, a VariabilityRepo instance represents a repository in which the extracted variability data was
 * stored. Each instance holds the information about which SPL commits were processed in which commit of the VariabilityRepo,
 * and whether the extraction of variability was successful for that commit.
 * </p>
 */
public class VariabilityRepo implements IVariabilityRepository {
    private Map<String, VariabilityCommit> allCommits;
    // Maps a VarCommit that processed the SPLCommit s to other VarCommits that processed the SPLCommits p_1 to p_n, where
    // p_1 to p_n are the parent commits of s in the history of the SPL repo
//    private Map<VariabilityCommit, VariabilityCommit[]> commitToEvolutionParents;
    private Set<VariabilityCommit> successCommits;
    private Set<VariabilityCommit> errorCommits;
    // Commits that did not process a merge in the SPL history. The set of nonMergeCommits is a subset of the union of
    // successCommits and errorCommits
    private Set<VariabilityCommit> nonMergeCommits;

    private final Path path;

    private VariabilityRepo(Path path) {
        this.path = path;
        Logger.status("Variability repository initialized from " + path);
    }

    /**
     * Load a repository by considering the commits in its history and the commits of the SPL that was processed.
     *
     * @param variabilityRepoDir The directory with the repository holding the variability data which was extracted from the processed SPL
     * @param splRepoDir         The directory with the repository of the processed SPL
     * @return A VariabilityRepo that holds meta data about the variability extraction
     * @throws IOException     If the provided files do not exist or cannot be accessed.
     * @throws GitAPIException If git cannot load the repositories' history correctly
     */
    public static VariabilityRepo load(File variabilityRepoDir, File splRepoDir) throws IOException, GitAPIException {
        // TODO: Implement Issue #13 here.
        Logger.status("Building VariabilityRepo instance for " + variabilityRepoDir);
        File currentCommitFile = new File(variabilityRepoDir, CURRENT_COMMIT_FILE);
        File errorFile = new File(variabilityRepoDir, ERROR_FILE);
        Iterable<RevCommit> commitIterable;

        Logger.debug("Loading commits of " + variabilityRepoDir);
        Git git = GitUtil.loadGitRepo(variabilityRepoDir);
        try {
            commitIterable = git.log().all().call();
        } catch (GitAPIException e) {
            Logger.exception("Was not able to load git log: ", e);
            throw e;
        }

        VariabilityRepo repo = new VariabilityRepo(variabilityRepoDir.toPath());
        Map<SPLCommit, VariabilityCommit> splCommitToVarCommit = new HashMap<>();
        Map<String, VariabilityCommit> allCommits = new HashMap<>();
        Set<VariabilityCommit> successCommits = new HashSet<>();
        Set<VariabilityCommit> errorCommits = new HashSet<>();
        for (var revCommit : commitIterable) {
            // Check out the commit
            try {
                Logger.debug("Checkout of commit " + revCommit.getName() + " in " + variabilityRepoDir);
                git.checkout().setName(revCommit.getName()).call();
            } catch (GitAPIException e) {
                Logger.exception("Was not able to checkout commit: ", e);
                throw e;
            }
            if (currentCommitFile.exists()) {
                String splCommitId;
                try {
                    splCommitId = TextIO.readLastLine(currentCommitFile);
                } catch (IOException e) {
                    Logger.exception("Was not able to retrieve information about processed commits: ", e);
                    throw e;
                }

                Logger.debug("Processed SPL commit " + splCommitId);
                SPLCommit splCommit = new SPLCommit(splCommitId);
                VariabilityCommit varCommit = new VariabilityCommit(repo, revCommit.getName(), splCommit);
                allCommits.put(varCommit.id(), varCommit);
                // Check whether the processed Linux commit resulted in an error
                if (errorFile.exists()) {
                    boolean hadError = false;
                    String[] errorCommitsList;
                    try {
                        errorCommitsList = TextIO.readLinesAsArray(errorFile);
                    } catch (IOException e) {
                        Logger.exception("Was not able to retrieve information about processed commits: ", e);
                        throw e;
                    }

                    for (var errorCommit : errorCommitsList) {
                        if (errorCommit.equals(splCommitId)) {
                            hadError = true;
                            break;
                        }
                    }

                    if (hadError) {
                        errorCommits.add(varCommit);
                        Logger.debug("The extraction of variability for SPL commit " + splCommitId + " had resulted in an error.");
                        continue;
                    }
                }

                successCommits.add(varCommit);
                Logger.debug("The extraction of variability for SPL commit " + splCommitId + " had been successful");

                // Map commit to Linux revision
                splCommitToVarCommit.put(splCommit, varCommit);
            }
        }
        repo.successCommits = successCommits;
        repo.errorCommits = errorCommits;
        repo.allCommits = allCommits;
        mapCommitsAccordingToSPLHistory(splCommitToVarCommit, splRepoDir, repo);
        return repo;
    }

    public VariabilityCommit getVariabilityCommit(String id) {
        return allCommits.get(id);
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
    public Set<CommitPair> getCommitPairsForEvolutionStudy() {
        return successCommits.stream()
                // We only consider commits that did not process a merge
                .filter(nonMergeCommits::contains)
                // We only consider commits that processed an SPL commit whose parent was also processed
                .filter(c -> {
                    VariabilityCommit[] parents = c.getEvolutionParents();
                    return parents.length == 1 && successCommits.contains(parents[0]);
                })
                .map(c -> new CommitPair(c, c.getEvolutionParents()[0]))
                .collect(Collectors.toSet());
    }

    /**
     * @return The set of commits for which the variability extraction was not successful
     */
    public Set<VariabilityCommit> getErrorCommits() {
        return new HashSet<>(errorCommits);
    }

    /**
     * @return The set of commits for which the variability extraction was successful
     */
    public Set<VariabilityCommit> getSuccessCommits() {
        return new HashSet<>(successCommits);
    }

    /**
     * @return The set of commits that did not process an SPLCommit that was a merge.
     */
    public Set<VariabilityCommit> getNonMergeCommits() {
        return nonMergeCommits;
    }

    private static void mapCommitsAccordingToSPLHistory(Map<SPLCommit, VariabilityCommit> splCommitToVarCommit, File splRepoDir, VariabilityRepo repo) throws IOException, GitAPIException {
        // TODO: Implement Issue #13 here.
        Logger.debug("Considering SPL history");
        Git git = GitUtil.loadGitRepo(splRepoDir);

        // Create a map of commits to their logical parents
        Set<SPLCommit> processedSPLCommits = splCommitToVarCommit.keySet();
        repo.nonMergeCommits = new HashSet<>();
        try {
            Logger.debug("Loading spl commits in " + splRepoDir);
            Logger.debug("Retrieving logical parents");
            for (var revCommit : git.log().all().call()) {
                final SPLCommit splCommit = new SPLCommit(revCommit.getName());
                if (processedSPLCommits.contains(splCommit)) {
                    splCommitToVarCommit.get(splCommit).setEvolutionParents(
                            // Process each parent commit in the SPL repo
                            Arrays.stream(revCommit.getParents())
                                    // Get the parent's SPLCommit representation
                                    .map(c -> new SPLCommit(c.getName()))
                                    // Only consider parent commits that were processed
                                    .filter(processedSPLCommits::contains)
                                    // Map the parent in the SPL repo to the commit that processed the parent
                                    .map(splCommitToVarCommit::get)
                                    // Collect the ids of all commits that processed a parent of the spl commit
                                    .toArray(VariabilityCommit[]::new));

                    if (revCommit.getParents().length == 1) {
                        repo.nonMergeCommits.add(splCommitToVarCommit.get(new SPLCommit(revCommit.getName())));
                    }
                }
            }
            Logger.debug("All logical parents retrieved.");
        } catch (GitAPIException e) {
            Logger.exception("Was not able to retrieve commits of SPL repo: ", e);
            throw e;
        }
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
    @Override
    public VariabilityHistory getCommitSequencesForEvolutionStudy() {
        // Retrieve the pairs of usable commits
        Set<CommitPair> usableCommitPairs = this.getCommitPairsForEvolutionStudy();
        // Create lists for the commits in the pairs and merge lists according to parent-child relationships
        Map<VariabilityCommit, LinkedList<VariabilityCommit>> commitToCommitSequenceMap = new HashMap<>();
        for (CommitPair pair : usableCommitPairs) {
            if (commitToCommitSequenceMap.containsKey(pair.parent()) && commitToCommitSequenceMap.containsKey(pair.child())) {
                // Parent and child already belong to a list
                // Merge the two lists, if they are not the same list
                var parentList = commitToCommitSequenceMap.get(pair.parent());
                var childList = commitToCommitSequenceMap.get(pair.child());
                if (parentList == childList) {
                    throw new IllegalStateException("The same parent-child pair was considered twice.");
                }
                // Add all commits from the child list to the parent list, and replace the list in the map
                parentList.addAll(childList);
                // Now, update the associated list for each added commit
                childList.forEach(c -> commitToCommitSequenceMap.put(c, parentList));
            } else if (commitToCommitSequenceMap.containsKey(pair.parent())) {
                // Only the parent belongs to a list
                // Append the child to the list
                var commitList = commitToCommitSequenceMap.get(pair.parent());
                commitList.addLast(pair.child());
                commitToCommitSequenceMap.put(pair.child(), commitList);
            } else if (commitToCommitSequenceMap.containsKey(pair.child())) {
                // Only the child belongs to a list
                // Prepend the parent to the list
                var commitList = commitToCommitSequenceMap.get(pair.child());
                commitList.addFirst(pair.parent());
                commitToCommitSequenceMap.put(pair.parent(), commitList);
            } else {
                // Neither parent nor child were added to a list
                // Create a new list that contains parent and child
                LinkedList<VariabilityCommit> commitList = new LinkedList<>();
                commitList.add(pair.parent());
                commitList.add(pair.child());
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

    @Override
    public Path getFeatureModelFile() {
        // TODO for Alex
        throw new NotImplementedException();
    }

    @Override
    public Path getVariabilityFile() {
        // TODO for Alex
        throw new NotImplementedException();
    }

    @Override
    public VariabilityCommit checkoutCommit(VariabilityCommit variabilityCommit) {
        // TODO: Implement Issue #12 here.
        throw new NotImplementedException();
    }

    @Override
    public void checkoutBranch(Branch branch) {
        // TODO: Implement Issue #12 here.
        throw new NotImplementedException();
    }

    @Override
    public VariabilityCommit getCurrentCommit() {
        // TODO: Implement Issue #12 here.
        throw new NotImplementedException();
    }

    @Override
    public Path getPath() {
        return path;
    }

}
