package de.variantsync.subjects;

import de.variantsync.io.TextIO;
import de.variantsync.util.GitUtil;
import de.variantsync.util.SimpleConsoleLogger;
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
    private static final SimpleConsoleLogger LOGGER = SimpleConsoleLogger.get();
    private Map<String, String> commitToSPLCommit;
    private Map<String, String[]> childParentMap;
    private Set<String> successCommits;
    private Set<String> errorCommits;
    // Commits that did not process a merge in the SPL history, but a normal commit
    private Set<String> normalCommits;

    private VariabilityRepo() {
        LOGGER.status("Variability repository initialized");
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
        LOGGER.status("Building VariabilityRepo instance for " + variabilityRepoDir);
        File currentCommitFile = new File(variabilityRepoDir, CURRENT_COMMIT_FILE);
        File errorFile = new File(variabilityRepoDir, ERROR_FILE);
        Iterable<RevCommit> commitIterable;

        LOGGER.debug("Loading commits of " + variabilityRepoDir);
        Git git = GitUtil.loadGitRepo(variabilityRepoDir);
        try {
            commitIterable = git.log().all().call();
        } catch (GitAPIException e) {
            LOGGER.exception("Was not able to load git log: ", e);
            throw e;
        }

        Map<String, String> eCommitToSPLCommit = new HashMap<>();
        Map<String, String> splCommitToECommit = new HashMap<>();
        Set<String> successCommits = new HashSet<>();
        Set<String> errorCommits = new HashSet<>();
        for (var commit : commitIterable) {
            // Check out the commit
            try {
                LOGGER.debug("Checkout of commit " + commit.getName() + " in " + variabilityRepoDir);
                git.checkout().setName(commit.getName()).call();
            } catch (GitAPIException e) {
                LOGGER.exception("Was not able to checkout commit: ", e);
                throw e;
            }
            if (currentCommitFile.exists()) {
                // Check whether the processed Linux commit resulted in an error
                String splCommit;
                try {
                    splCommit = TextIO.readLastLine(currentCommitFile);
                    LOGGER.debug("Processed SPL commit " + splCommit);
                    if (errorFile.exists()) {
                        boolean hadError = false;
                        for (var errorCommit : TextIO.readLinesAsArray(errorFile)) {
                            if (errorCommit.equals(splCommit)) {
                                hadError = true;
                                break;
                            }
                        }
                        if (hadError) {
                            errorCommits.add(commit.getName());
                            LOGGER.debug("The extraction of variability for SPL commit " + splCommit + " had resulted in an error.");
                        } else {
                            successCommits.add(commit.getName());
                            LOGGER.debug("The extraction of variability for SPL commit " + splCommit + " had been successful");
                        }
                    } else {
                        successCommits.add(commit.getName());
                        LOGGER.debug("The extraction of variability for SPL commit " + splCommit + " had been successful");
                    }
                } catch (IOException e) {
                    LOGGER.exception("Was not able to retrieve information about processed commits: ", e);
                    throw e;
                }
                // Map commit to Linux revision
                eCommitToSPLCommit.put(commit.getName(), splCommit);
                splCommitToECommit.put(splCommit, commit.getName());
            }
        }
        VariabilityRepo repo = new VariabilityRepo();
        repo.successCommits = successCommits;
        repo.errorCommits = errorCommits;
        considerSPLHistory(splCommitToECommit, splRepoDir, repo);
        repo.commitToSPLCommit = eCommitToSPLCommit;
        return repo;
    }

    /**
     * Each commit in the variability repo was responsible for processing one commit from the SPL repo. This method
     * returns the commits from the variability repo that processed the parent commits in the SPL repo of the SPL commit
     * that was processed by the provided commit.
     * <p>
     * Note that these are NOT the parents of the commit in the variability repository.
     *
     * @param commit A commit from the variability repo
     * @return Array of commits that processed the parent commits in the SPL history
     */
    public String[] getExtractionParents(String commit) {
        return childParentMap.get(commit);
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
                .filter(normalCommits::contains)
                // We only consider commits that processed an SPL commit whose parent was also processed
                .filter((c) -> {
                    String[] parents = childParentMap.get(c);
                    return parents.length == 1 && successCommits.contains(parents[0]);
                })
                .map((c) -> new CommitPair(c, childParentMap.get(c)[0]))
                .collect(Collectors.toSet());
    }

    /**
     * Get the SPL commit that was processed by the given commit from the variability repo
     *
     * @param commit A commit from this variability repo
     * @return The id of the SPL commit that was processed
     */
    public String getSPLCommit(String commit) {
        return commitToSPLCommit.get(commit);
    }

    /**
     * @return The set of commits for which the variability extraction was not successful
     */
    public Set<String> getErrorCommits() {
        return new HashSet<>(errorCommits);
    }

    /**
     * @return The set of commits for which the variability extraction was successful
     */
    public Set<String> getSuccessCommits() {
        return new HashSet<>(successCommits);
    }

    public static record CommitPair(String child, String parent) {}

    private static void considerSPLHistory(Map<String, String> splCommitToECommit, File splRepoDir, VariabilityRepo repo) throws IOException, GitAPIException {
        LOGGER.debug("Considering SPL history");
        Git git = GitUtil.loadGitRepo(splRepoDir);

        // Create a map of commits to their logical parents
        repo.childParentMap = new HashMap<>();
        Set<String> processedSPLCommits = splCommitToECommit.keySet();
        repo.normalCommits = new HashSet<>();
        try {
            LOGGER.debug("Loading spl commits in " + splRepoDir);
            LOGGER.debug("Retrieving logical parents");
            for (var commit : git.log().all().call()) {
                if (processedSPLCommits.contains(commit.getName())) {
                    repo.childParentMap.put(splCommitToECommit.get(commit.getName()),
                            // Process each parent commit in the SPL repo
                            Arrays.stream(commit.getParents())
                                    // Get the parent's id
                                    .map(RevCommit::getName)
                                    // Only consider parent commits that were processed
                                    .filter(processedSPLCommits::contains)
                                    // Map the parent in the SPL repo to the commit that processed the parent
                                    .map(splCommitToECommit::get)
                                    // Collect the ids of all commits that processed a parent of the spl commit
                                    .toArray(String[]::new));

                    if (commit.getParents().length == 1) {
                        repo.normalCommits.add(splCommitToECommit.get(commit.getName()));
                    }
                }
            }
            LOGGER.debug("All logical parents retrieved.");
        } catch (GitAPIException e) {
            LOGGER.exception("Was not able to retrieve commits of SPL repo: ", e);
            throw e;
        }
    }
}
