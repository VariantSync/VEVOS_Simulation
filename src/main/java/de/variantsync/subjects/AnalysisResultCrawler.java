package de.variantsync.subjects;

import de.variantsync.io.TextIO;
import de.variantsync.util.GitUtil;
import de.variantsync.util.SimpleConsoleLogger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.variantsync.subjects.Constants.*;

public class AnalysisResultCrawler {
    private static final SimpleConsoleLogger LOGGER = SimpleConsoleLogger.get();

    public static void crawl(File repoDir) throws GitAPIException, IOException {
        // Map of extraction repo commits to SPL commits, if a result is available
        Map<String, String> eCommitToSPLCommit = getECommitToSPLCommitMap(repoDir);

        // Filter out merges

        System.out.println(eCommitToSPLCommit);
    }

    private static Map<String, String> getECommitToSPLCommitMap(File repoDir) throws IOException, GitAPIException {
        File currentCommitFile = new File(repoDir, CURRENT_COMMIT_FILE);
        File errorFile = new File(repoDir, ERROR_FILE);
        Iterable<RevCommit> commitIterable;

        Git git = GitUtil.loadGitRepo(repoDir);
        try {
            commitIterable = git.log().all().call();
        } catch (GitAPIException e) {
            LOGGER.exception("Was not able to load git log: ", e);
            throw e;
        }

        Map<String, String> analysisCommitToLinuxCommit = new HashMap<>();
        OUTER_LOOP:
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
                            // We do not want to process this commit and skip it
                            continue OUTER_LOOP;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.exception("Was not able to retrieve information about processed commits: ", e);
                throw e;
            }
            // Map commit to Linux revision
            analysisCommitToLinuxCommit.put(commit.getName(), splCommit);
        }
        return analysisCommitToLinuxCommit;
    }

    private static Map<String, String> filterMerges(Map<String, String> eCommitToSPLCommitMap, File splDir) throws IOException {
        Git git = GitUtil.loadGitRepo(splDir);

        // Create a map of spl commits to their parents
        Map<String, String[]> commitToParentsMap = new HashMap<>();
        try {
            for(var commit : git.log().all().call()) {
                commitToParentsMap.put(commit.getName(), commit.getParents())
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        for (var entry : eCommitToSPLCommitMap.entrySet()) {
            // Get the parents of the SPL commit

            RevCommit splCommit = git.getRepository().resolve(entry.getValue()).;

        }
    }

}
