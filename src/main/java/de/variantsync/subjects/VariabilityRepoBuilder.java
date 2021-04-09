package de.variantsync.subjects;

import de.variantsync.io.TextIO;
import de.variantsync.util.GitUtil;
import de.variantsync.util.SimpleConsoleLogger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static de.variantsync.subjects.Constants.*;

public class VariabilityRepoBuilder {
    private static final SimpleConsoleLogger LOGGER = SimpleConsoleLogger.get();

    public static VariabilityRepo build(File extractionRepoDir, File splRepoDir) throws IOException, GitAPIException {
        LOGGER.status("Building VariabilityRepo instance for " + extractionRepoDir);
        File currentCommitFile = new File(extractionRepoDir, CURRENT_COMMIT_FILE);
        File errorFile = new File(extractionRepoDir, ERROR_FILE);
        Iterable<RevCommit> commitIterable;

        LOGGER.debug("Loading commits of " + extractionRepoDir);
        Git git = GitUtil.loadGitRepo(extractionRepoDir);
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
                LOGGER.debug("Checkout of commit " + commit.getName() + " in " + extractionRepoDir);
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
                        if(hadError) {
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
        return new VariabilityRepo(eCommitToSPLCommit, getLogicalParentsMap(splCommitToECommit, splRepoDir), successCommits, errorCommits);
    }

    private static Map<String, String[]> getLogicalParentsMap(Map<String, String> splCommitToECommit, File splRepoDir) throws IOException, GitAPIException {
        LOGGER.debug("Creating logical parents map.");
        Git git = GitUtil.loadGitRepo(splRepoDir);

        // Create a map of commits to their logical parents
        Map<String, String[]> commitToLogicalParentsMap = new HashMap<>();
        Set<String> processedSPLCommits = splCommitToECommit.keySet();
        try {
            LOGGER.debug("Loading spl commits in " + splRepoDir);
            LOGGER.debug("Retrieving logical parents");
            for (var commit : git.log().all().call()) {
                if (processedSPLCommits.contains(commit.getName())) {
                    commitToLogicalParentsMap.put(splCommitToECommit.get(commit.getName()),
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

                }
            }
            LOGGER.debug("All logical parents retrieved.");
            return commitToLogicalParentsMap;
        } catch (GitAPIException e) {
            LOGGER.exception("Was not able to retrieve commits of SPL repo: ", e);
            throw e;
        }
    }

}
