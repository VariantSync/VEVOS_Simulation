package de.variantsync.subjects;

import de.variantsync.io.TextIO;
import de.variantsync.util.GitUtil;
import de.variantsync.util.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.lang.String;

import java.util.*;
import java.util.stream.Collectors;

import static de.variantsync.subjects.Constants.CURRENT_COMMIT_FILE;
import static de.variantsync.subjects.Constants.ERROR_FILE;

/**
 * Record for storing meta data about the SPL commits that were processed by a variability extraction.
 * <p>
 * More specifically, a VariabilityRepo instance represents a repository in which the extracted variability data was
 * stored. Each instance holds the information about which SPL commits were processed in which commit of the VariabilityRepo,
 * and whether the extraction of variability was successful for that commit.
 * </p>
 */
public class VariabilityRepo {
    private Map<VarCommit, SPLCommit> commitToSPLCommit;
    // Maps a VarCommit that processed the SPLCommit s to other VarCommits that processed the SPLCommits p_1 to p_n, where
    // p_1 to p_n are the parent commits of s in the history of the SPL repo
    private Map<VarCommit, VarCommit[]> commitToEvolutionParents;
    private Set<VarCommit> successCommits;
    private Set<VarCommit> errorCommits;
    // Commits that did not process a merge in the SPL history. The set of nonMergeCommits is a subset of the union of
    // successCommits and errorCommits
    private Set<VarCommit> nonMergeCommits;

    private VariabilityRepo() {
        Logger.status("Variability repository initialized");
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

        Map<VarCommit, SPLCommit> varCommitToSPLCommit = new HashMap<>();
        Map<SPLCommit, VarCommit> splCommitToVarCommit = new HashMap<>();
        Set<VarCommit> successCommits = new HashSet<>();
        Set<VarCommit> errorCommits = new HashSet<>();
        for (var revCommit : commitIterable) {
            VarCommit varCommit = new VarCommit(revCommit.getName());
            // Check out the commit
            try {
                Logger.debug("Checkout of commit " + revCommit.getName() + " in " + variabilityRepoDir);
                git.checkout().setName(revCommit.getName()).call();
            } catch (GitAPIException e) {
                Logger.exception("Was not able to checkout commit: ", e);
                throw e;
            }
            if (currentCommitFile.exists()) {
                // Check whether the processed Linux commit resulted in an error
                String splCommitId;
                try {
                    splCommitId = TextIO.readLastLine(currentCommitFile);
                    Logger.debug("Processed SPL commit " + splCommitId);
                    if (errorFile.exists()) {
                        boolean hadError = false;
                        for (var errorCommit : TextIO.readLinesAsArray(errorFile)) {
                            if (errorCommit.equals(splCommitId)) {
                                hadError = true;
                                break;
                            }
                        }
                        if (hadError) {
                            errorCommits.add(varCommit);
                            Logger.debug("The extraction of variability for SPL commit " + splCommitId + " had resulted in an error.");
                        } else {
                            successCommits.add(varCommit);
                            Logger.debug("The extraction of variability for SPL commit " + splCommitId + " had been successful");
                        }
                    } else {
                        successCommits.add(varCommit);
                        Logger.debug("The extraction of variability for SPL commit " + splCommitId + " had been successful");
                    }
                } catch (IOException e) {
                    Logger.exception("Was not able to retrieve information about processed commits: ", e);
                    throw e;
                }
                SPLCommit splCommit = new SPLCommit(splCommitId);
                // Map commit to Linux revision
                varCommitToSPLCommit.put(varCommit, splCommit);
                splCommitToVarCommit.put(splCommit, varCommit);
            }
        }
        VariabilityRepo repo = new VariabilityRepo();
        repo.successCommits = successCommits;
        repo.errorCommits = errorCommits;
        mapCommitsAccordingToSPLHistory(splCommitToVarCommit, splRepoDir, repo);
        repo.commitToSPLCommit = varCommitToSPLCommit;
        return repo;
    }

    /**
     * Each commit in the variability repo was responsible for processing one commit from the SPL repo. This method
     * returns the commits from the variability repo that processed the parent commits in the SPL repo of the SPL commit
     * that was processed by the provided commit.
     * <p>
     * Note that these are NOT the parents of the commit in the variability repository.
     *
     * @param varCommit A commit from the variability repo
     * @return Commits that processed the parent commits in the SPL history
     */
    public VarCommit[] getEvolutionParents(VarCommit varCommit) {
        return commitToEvolutionParents.get(varCommit);
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
                    VarCommit[] parents = commitToEvolutionParents.get(c);
                    return parents.length == 1 && successCommits.contains(parents[0]);
                })
                .map(c -> new CommitPair(c, commitToEvolutionParents.get(c)[0]))
                .collect(Collectors.toSet());
    }

    /**
     * Get the SPL commit that was processed by the given commit from the variability repo
     *
     * @param varCommit A commit from this variability repo
     * @return The id of the SPL commit that was processed
     */
    public SPLCommit getSPLCommit(VarCommit varCommit) {
        return commitToSPLCommit.get(varCommit);
    }

    /**
     * @return The set of commits for which the variability extraction was not successful
     */
    public Set<VarCommit> getErrorCommits() {
        return new HashSet<>(errorCommits);
    }

    /**
     * @return The set of commits for which the variability extraction was successful
     */
    public Set<VarCommit> getSuccessCommits() {
        return new HashSet<>(successCommits);
    }

    /**
     * @return The set of commits that did not process an SPLCommit that was a merge.
     */
    public Set<VarCommit> getNonMergeCommits() {
        return nonMergeCommits;
    }

    private static void mapCommitsAccordingToSPLHistory(Map<SPLCommit, VarCommit> splCommitToVarCommit, File splRepoDir, VariabilityRepo repo) throws IOException, GitAPIException {
        Logger.debug("Considering SPL history");
        Git git = GitUtil.loadGitRepo(splRepoDir);

        // Create a map of commits to their logical parents
        repo.commitToEvolutionParents = new HashMap<>();
        Set<SPLCommit> processedSPLCommits = splCommitToVarCommit.keySet();
        repo.nonMergeCommits = new HashSet<>();
        try {
            Logger.debug("Loading spl commits in " + splRepoDir);
            Logger.debug("Retrieving logical parents");
            for (var revCommit : git.log().all().call()) {
                if (processedSPLCommits.contains(new SPLCommit(revCommit.getName()))) {
                    repo.commitToEvolutionParents.put(splCommitToVarCommit.get(new SPLCommit(revCommit.getName())),
                            // Process each parent commit in the SPL repo
                            Arrays.stream(revCommit.getParents())
                                    // Get the parent's SPLCommit representation
                                    .map(c -> new SPLCommit(c.getName()))
                                    // Only consider parent commits that were processed
                                    .filter(processedSPLCommits::contains)
                                    // Map the parent in the SPL repo to the commit that processed the parent
                                    .map(splCommitToVarCommit::get)
                                    // Collect the ids of all commits that processed a parent of the spl commit
                                    .toArray(VarCommit[]::new));

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
}
