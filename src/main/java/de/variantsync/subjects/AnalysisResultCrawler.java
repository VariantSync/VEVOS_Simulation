package de.variantsync.subjects;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class AnalysisResultCrawler {

    public static void crawl(Path pathToRepo) {
        Iterable<RevCommit> commitIterable;
        Repository repository = null;
        try {
            repository = new FileRepositoryBuilder()
                    .setGitDir(new File(pathToRepo.toFile(), ".git"))
                    .build();
        } catch (IOException e) {
            // TODO: Handle
            return;
        }
        Git git = new Git(repository);
        try {
            commitIterable = git.log().all().call();
        } catch (GitAPIException | IOException e) {
            // TODO: Handle
            return;
        }
        // Check out the commit
        // Check whether the commit resulted in an error
        // Map commit to Linux revision

    }
}
