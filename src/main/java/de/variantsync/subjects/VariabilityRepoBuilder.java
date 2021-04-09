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
    private final File extractionRepoDir;
    private final File splRepoDir;

    public VariabilityRepoBuilder(File extractionRepoDir, File splRepoDir) {
        this.extractionRepoDir = extractionRepoDir;
        this.splRepoDir = splRepoDir;
    }

    public VariabilityRepo build() throws IOException, GitAPIException {
        File currentCommitFile = new File(extractionRepoDir, CURRENT_COMMIT_FILE);
        File errorFile = new File(extractionRepoDir, ERROR_FILE);
        Iterable<RevCommit> commitIterable;

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
                git.checkout().setName(commit.getName()).call();
            } catch (GitAPIException e) {
                LOGGER.exception("Was not able to checkout commit: ", e);
                throw e;
            }

            // Check whether the processed Linux commit resulted in an error
            String splCommit;
            try {
                splCommit = TextIO.readLastLine(currentCommitFile);

                if (errorFile.exists()) {
                    for (var errorCommit : TextIO.readLinesAsArray(errorFile)) {
                        if (errorCommit.equals(splCommit)) {
                            errorCommits.add(splCommit);
                        } else {
                            successCommits.add(splCommit);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.exception("Was not able to retrieve information about processed commits: ", e);
                throw e;
            }
            // Map commit to Linux revision
            eCommitToSPLCommit.put(commit.getName(), splCommit);
            splCommitToECommit.put(splCommit, commit.getName());
        }
        return new VariabilityRepo(eCommitToSPLCommit, getLogicalParentsMap(splCommitToECommit, splRepoDir), successCommits, errorCommits);
    }

    private Map<String, String[]> getLogicalParentsMap(Map<String, String> splCommitToECommit, File splDir) throws IOException, GitAPIException {
        Git git = GitUtil.loadGitRepo(splDir);

        // Create a map of commits to their logical parents
        Map<String, String[]> commitToLogicalParentsMap = new HashMap<>();
        Set<String> processedSPLCommits = splCommitToECommit.keySet();
        try {
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
            return commitToLogicalParentsMap;
        } catch (GitAPIException e) {
            LOGGER.exception("Was not able to retrieve commits of SPL repo: ", e);
            throw e;
        }
    }

}
